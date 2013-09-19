package com.smallcrafts.wakemeup;

import com.smallcrafts.wakemeup.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class ServiceActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	private static CustomAddress destinationAddress;
	private static SharedPreferences sharedPref;
	// Threshold in meters to easy calculations
	private static int thresholdDistance;
	private static float distance = 0;
	private static float initialDistance = 0;
	private static String textUnits = "Km";
	private static boolean vibrator;
	private static boolean sound;
	private static boolean snooze;
	private static boolean units;
	private static Location myLocation;
	private static TextView distanceText;
	private static TextView unitText;
	private static TextView locationText;
	private static Button dismissButton;
	private static LocationManager locationManager;
	private static Ringtone notificationSound;
	private static Vibrator notificationVibrator;
	private static NotificationManager notificationManager;
	private static int snoozeCounter = 0;
	public static final int NOTIFICATIONID = 110101001;
	private static BroadcastReceiver breceiver;
	private static double[] latlng = new double[2];
	private static boolean activityStatus = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_service);
		setupActionBar();
		
		myLocation = getCurrentLocation();
		restoreSettings();
		
		if (units){
			textUnits = getString(R.string.miles);
		}
		
		Intent i = getIntent();
		Address a = (Address) i.getParcelableExtra("com.smallcrafts.wakemeup.destination");
		if (a != null){
			destinationAddress = new CustomAddress(a);
			Log.d("DAEMON", "Destination Address: " + destinationAddress.toString());
		}
		
		//Start service
		Intent daemonIntent = new Intent(ServiceActivity.this, LocationDaemon.class);
		daemonIntent.putExtra("com.smallcrafts.wakemeup.destination", (Address)destinationAddress);
		startService(daemonIntent);
		
		// LocationDaemon update receiver
		breceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent intent) {
			    // Extract data included in the Intent
				latlng = intent.getDoubleArrayExtra("LatLng");
				distance = intent.getFloatExtra("Distance", 0);
				Log.d("SERVICE", "Location Update. Latitude: "+ Double.toString(latlng[0]) + " - Longitude: " + Double.toString(latlng[1]));
				Log.d("SERVICE", "Current Distance: " + Float.toString(distance));
				Log.d("SERVICE", "Thershold: " + Integer.toString(thresholdDistance));
//				calculateDistance();
				updateUI();
				
				//TODO Algorithm for minimizing location access
				
				float comparableDistance;
				
				if (units){
					comparableDistance = (float) ((float) distance*0.621371);
				} else {
					comparableDistance = distance;
				}
				
				Log.d("SERVICE", "Comparable Distance: " + Float.toString(comparableDistance));
				
				if (comparableDistance < thresholdDistance){
					if (!snooze || (snoozeCounter > 1)){
						Log.d("SERVICE", "Location Updates removed.");
						stopService(new Intent(ServiceActivity.this, LocationDaemon.class));
						
					}
					
					Log.d("SERVICE","SnoozeCounter : " + Integer.toString(snoozeCounter));
					Log.d("SERVICE","Current ThresholdDistance: " + Integer.toString(thresholdDistance));
					
					// TEST ON DAEMON + ALARM ARCHITECTURE
					//notifyArrival();
				}
			}
		};
		
		LocalBroadcastManager.getInstance(this).registerReceiver(breceiver,
				new IntentFilter("com.smallcrafts.wakemeup.update"));
		
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.content);
		distanceText = (TextView) findViewById(R.id.distance_text);
		unitText = (TextView) findViewById(R.id.unit_text);
		locationText = (TextView) findViewById(R.id.location_text);
		
		locationText.setText(destinationAddress.toString());
		
		calculateDistance();
		initialDistance = distance;
		updateUI();

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		
		
		dismissButton = (Button) findViewById(R.id.dismiss_button);
		dismissButton.setOnTouchListener(mDelayHideTouchListener);
		dismissButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
//				stopNotifications();
//				if(snooze && snoozeCounter < 2){
//					snoozeCounter++;
//					thresholdDistance = (int) thresholdDistance/2;
//				}
			}
			
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		activityStatus = true;
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		removeOSNotification();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		activityStatus = false;
		Log.d("SERVICE", "Activity goes into PAUSED status.");
	}
	
	@Override
	public void onUserLeaveHint(){
		super.onUserLeaveHint();
		activityStatus = false;
		launchOSNotification();
		Log.d("SERVICE", "User leaves the app (on Background).");
	} 
	
//	private void stopNotifications(){
//		Log.d("SERVICE", "Stopping all non screen notifications");
//		if (notificationSound != null){
//			notificationSound.stop();
//			notificationSound = null;
//		}
//		if (notificationVibrator != null){
//			notificationVibrator.cancel();
//			notificationVibrator = null;
//		}
//	}
	
//	private void notifyArrival(){
//		Log.d("SERVICE","Non Screen Notifications Launched.");
//		
//		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//		int ringDuration;
//		ringDuration = MediaPlayer.create(getApplicationContext(), notification).getDuration();
//		
//		long division = Math.round(ringDuration/8);
//		long[] pattern = {0, division * 7, division * 2};
//		
//		if (notificationSound == null && sound){
//			notificationSound = RingtoneManager.getRingtone(getApplicationContext(), notification);
//			notificationSound.play();
//		}
//		
//		if (notificationVibrator == null && vibrator){
//			notificationVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//			if (notificationVibrator.hasVibrator())
//				notificationVibrator.vibrate(pattern, 0);
//		}
//		mSystemUiHider.show();
//	}
	
	private void updateUI(){
		if (units){
			unitText.setText(getString(R.string.miles));
		} else{
			unitText.setText(getString(R.string.kilometers));
		}
		
		long printableDistance = 0;
		
		if(distance != 0){
			if (units){
				printableDistance = Math.round((distance/1000)*0.621371);
			} else {
				printableDistance = Math.round(distance/1000);
			}
		}
		
		if (printableDistance > 9999){
			distanceText.setTextSize(100);
		} else if (printableDistance > 999){
			distanceText.setTextSize(125);
		} else {
			distanceText.setTextSize(150);
		}
		
		distanceText.setText(Long.toString(printableDistance));
		Log.d("SERVICE", "UI Updated");
		
	}
	
	private void calculateDistance(){
		if (myLocation != null){
			float[] results = new float[1];
			try{
				Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(), destinationAddress.getLatitude(), destinationAddress.getLongitude(), results);
				distance = results[0];
			} catch (IllegalArgumentException e) {
				
			}
		}
	}
	
	private void restoreSettings(){
		sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		thresholdDistance =(int)(sharedPref.getFloat("distance", 5)*1000.0);
		Log.d("SERVICE", "Thershold: " + Integer.toString(thresholdDistance));
		vibrator = sharedPref.getBoolean("vibrator", true);
		sound = sharedPref.getBoolean("sound", true);
		snooze = sharedPref.getBoolean("snooze", false);
		units = sharedPref.getBoolean("units", true);
	}
	
	private Location getCurrentLocation(){
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
	}
	
	public void launchOSNotification(){
		long unitDistance = 0;
		if(distance != 0){
			if (units){
				unitDistance = Math.round((distance/1000)*0.621371);
			} else {
				unitDistance = Math.round(distance/1000);
			}
		}
		
		String message = Long.toString(unitDistance) + " " + textUnits + " left to get there.";
		
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
	    .setSmallIcon(R.drawable.ic_launcher)
	    .setContentTitle("On our Way!")
	    .setContentText(message)
	    .setOngoing(true);
		
		Intent resultIntent = new Intent(this, ServiceActivity.class);
		resultIntent.putExtra("com.smallcrafts.wakemeup.destination", (Address)destinationAddress);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(ServiceActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		notificationBuilder.setContentIntent(resultPendingIntent);
		
		
		notificationManager.notify(NOTIFICATIONID, notificationBuilder.build());
	}
	
	private void removeOSNotification(){
		notificationManager.cancel(NOTIFICATIONID);
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	public static boolean isRunning() {
	    return activityStatus;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
}
