package com.smallcrafts.wakemeup;

import android.location.Address;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;

public class AlarmActivity extends Activity {

	private static TextView distanceText;
	private static TextView unitText;
	private static TextView locationText;
	private static Button dismissButton;
	private static Button snoozeButton;
	private static Ringtone notificationSound;
	private static Vibrator notificationVibrator;
	private static SharedPreferences sharedPref;
	private static NotificationManager notificationManager;
	private static CustomAddress destinationAddress = null;
	private static String textUnits =null;
	private static float distance=0;
	private static boolean vibrator;
	private static boolean sound;
	private static boolean snooze;
	private static boolean units;
	private static boolean activityStatus = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		
		Intent receivedIntent = getIntent();
		
		Address a = (Address) receivedIntent.getParcelableExtra("com.smallcrafts.wakemeup.destination");
		if (a != null)
				destinationAddress = new CustomAddress(a);
		distance = receivedIntent.getFloatExtra("com.smallcrafts.wakemeup.distance", distance);
		restoreSettings();
		
		notifyArrival();
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		distanceText = (TextView) findViewById(R.id.alarm_distance_text);
		long printableDistance = 0;
		
		if(distance != 0){
			if (units){
				printableDistance = Math.round((distance/1000)*0.621371);
			} else {
				printableDistance = Math.round(distance/1000);
			}
		}
		distanceText.setText(Long.toString(printableDistance));
		
		unitText = (TextView) findViewById(R.id.alarm_unit_text);
		if (units){
			unitText.setText(getString(R.string.miles));
		} else{
			unitText.setText(getString(R.string.kilometers));
		}
		
		locationText = (TextView) findViewById(R.id.alarm_location_text);
		locationText.setText(destinationAddress.toString());
		
		dismissButton = (Button) findViewById(R.id.alarm_dismiss_button);
		dismissButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				stopService(new Intent(AlarmActivity.this, LocationDaemon.class));
//				notificationManager.cancel(ServiceActivity.NOTIFICATIONID);
				stopNotifications();
				Log.d("DAEMON", "Dismissed. Location Updates removed.");
				finish();
			}
			
		});
		
		snoozeButton = (Button) findViewById(R.id.alarm_snooze_button);
		if (snooze){
			snoozeButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					stopNotifications();
					Log.d("ALARM", "Snoozed. Location Updates removed.");
					finish();
				}
			});
		} else {
			snoozeButton.setVisibility(Button.GONE);
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		activityStatus = true;
		notificationManager.cancel(ServiceActivity.NOTIFICATIONID);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		activityStatus = false;
	}
	
	public static boolean isRunning() {
	    return activityStatus;
	}
	
	private void stopNotifications(){
		Log.d("ALARM", "Stopping all non screen notifications");
		if (notificationSound != null){
			notificationSound.stop();
			notificationSound = null;
		}
		if (notificationVibrator != null){
			notificationVibrator.cancel();
			notificationVibrator = null;
		}
	}
	
	private void notifyArrival(){
		Log.d("ALARM","Non Screen Notifications Launched.");
		
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		int ringDuration;
		ringDuration = MediaPlayer.create(getApplicationContext(), notification).getDuration();
		
		long division = Math.round(ringDuration/8);
		long[] pattern = {0, division * 7, division * 2};
		
		if (notificationSound == null && sound){
			notificationSound = RingtoneManager.getRingtone(getApplicationContext(), notification);
			notificationSound.play();
		}
		
		if (notificationVibrator == null && vibrator){
			notificationVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			if (notificationVibrator.hasVibrator())
				notificationVibrator.vibrate(pattern, 0);
		}
	}
	
	private void restoreSettings(){
		sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		vibrator = sharedPref.getBoolean("vibrator", true);
		sound = sharedPref.getBoolean("sound", true);
		snooze = sharedPref.getBoolean("snooze", false);
		units = sharedPref.getBoolean("units", true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarm, menu);
		return true;
	}

}
