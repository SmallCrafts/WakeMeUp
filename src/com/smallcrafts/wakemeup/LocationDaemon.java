package com.smallcrafts.wakemeup;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

public class LocationDaemon extends Service implements LocationListener {

	private static LocationManager locationManager;
	private static Criteria criteria;
	private static Intent com;
	private static NotificationManager notificationManager;
	private static CustomAddress destinationAddress = null;
	// 0. location latitude , 1. location longitude
	private static double[] latlng = new double[2];
	private static float[] results = new float[1];
	private static SharedPreferences sharedPref;
	// Threshold in meters to easy calculations
	private static int thresholdDistance;
	private static int snoozeCounter = 0;
	private static float distance = 0;
	private static float initialDistance = 0;
	private static int locationUpdateRate = 5;
	private static long unitDistance = 0;
	private static long lastDistance = 0;
	private static boolean vibrator;
	private static boolean sound;
	private static boolean snooze;
	private static boolean units;
	private static String unitText = "Km";
	public static final int NOTIFICATIONID = 110101001;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){
		
		restoreSettings();
		
		if (units)
			unitText = getString(R.string.miles);
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	    criteria.setAltitudeRequired(false);
	    criteria.setBearingRequired(false);
	    criteria.setCostAllowed(false);
	    criteria.setSpeedRequired(false);
	    criteria.setPowerRequirement(Criteria.POWER_LOW);
		locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), locationUpdateRate, 0, this);
	}
	
	@Override
	public int onStartCommand(Intent i, int flags, int startId){
		Address a = (Address) i.getParcelableExtra("com.smallcrafts.wakemeup.destination");
		if (a != null){
			destinationAddress = new CustomAddress(a);
			Log.d("DAEMON", "Destination Address: " + destinationAddress.toString());
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		destinationAddress = null;
		locationManager.removeUpdates(this);
		Log.d("DAEMON", "Service stopped!");
	}

	private void launchAlarm(){
		Intent alarmIntent = new Intent(this, AlarmActivity.class);
		alarmIntent.putExtra("com.smallcrafts.wakemeup.destination", (Address)destinationAddress);
		alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(alarmIntent);	
	}
	
	private void checkUpdateFrequency(){
		int currentUpdateRate = locationUpdateRate;
		
		if (distance > initialDistance/2){
			if (distance > ((3*initialDistance)/4)){
				if(currentUpdateRate != 10){
					currentUpdateRate = 10;
				}
			} else if (currentUpdateRate != 7){
				currentUpdateRate = 7;
			}
		} else if (currentUpdateRate != 5){
			currentUpdateRate = 5;
		}
		
		updateLocationRequestFrecuency(currentUpdateRate);
	}
	
	private void updateLocationRequestFrecuency (int f){
		Log.d("DAEMON", "Current location update rate: " + Integer.toString(locationUpdateRate) + " min.");
		Log.d("DAEMON", "NEW location update rate: " + Integer.toString(f) + " min.");
		if (f != locationUpdateRate){
			locationUpdateRate = f;
			locationManager.removeUpdates(this);
			locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), locationUpdateRate, 0, this);
		}
	}
	
	private void calculateDistance(Location l){
		if (destinationAddress != null){
			try{
				Location.distanceBetween(l.getLatitude(), l.getLongitude(), destinationAddress.getLatitude(), destinationAddress.getLongitude(), results);
				distance = results[0];
				if (initialDistance  == 0){
					initialDistance = distance;
				}
			} catch (IllegalArgumentException e) {
				
			}
		}
	}
	
	private void checkNotification(boolean forced){
		sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		units = sharedPref.getBoolean("units", true);
		
		if (units)
			unitText = getString(R.string.miles);
		else
			unitText = getString(R.string.kilometers);
		
		if(distance != 0){
			if (units){
				unitDistance = Math.round((distance/1000)*0.621371);
			} else {
				unitDistance = Math.round(distance/1000);
			}
		}
		
		Log.d("DAEMON", "Unit Distance: " + Long.toString(unitDistance));
		Log.d("DAEMON", "Last Distance: " + Long.toString(lastDistance));
		Log.d("DAEMON", "UI Activity Status: " + Boolean.toString(isUiVisible()));
		
		if (((lastDistance != unitDistance) || forced)  && !isUiVisible()){	
			Log.d("DAEMON", "Notification Updated ...");
			lastDistance = unitDistance;
			launchOSNotification(this);
		}
	}
	
	private void restoreSettings(){
		sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		thresholdDistance =(int)(sharedPref.getFloat("distance", 5)*1000.0);
		Log.d("DAEMON", "Thershold: " + Integer.toString(thresholdDistance));
		vibrator = sharedPref.getBoolean("vibrator", true);
		sound = sharedPref.getBoolean("sound", true);
		snooze = sharedPref.getBoolean("snooze", false);
		units = sharedPref.getBoolean("units", true);
	}
	
	public static CustomAddress isRunning(){
		return destinationAddress;
	}
	
	public static void removeOSNotification(){
		notificationManager.cancel(NOTIFICATIONID);
	}
	
	public static void launchOSNotification(Context context){
		Log.d("DAEMON", "Forced Notification Update!!!");
		if(!isUiVisible()){
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
		    .setSmallIcon(R.drawable.ic_stat_notify_wmu)
		    .setContentTitle("On our Way!")
		    .setContentText(Long.toString(unitDistance) + " " + unitText + " left to get there.")
		    .setOngoing(true);
			
			NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
			bigTextStyle  = bigTextStyle.bigText(Html.fromHtml("<p>" + destinationAddress.toString() + "</p>" + Long.toString(unitDistance) + " " + unitText + " left to get there."));
			notificationBuilder.setStyle(bigTextStyle);
			
			Intent resultIntent = new Intent(context, ServiceActivity.class);
			resultIntent.putExtra("com.smallcrafts.wakemeup.destination", (Address)destinationAddress);
			
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
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
	}
	
	private static boolean isUiVisible(){
		return (MainMenu.mmenuVisible || LocationActivity.locationVisible || SettingsActivity.settingsVisible || ServiceActivity.serviceVisible || AlarmActivity.alarmVisible );
	}
	
	@Override
	public void onLocationChanged(Location location) {
		calculateDistance(location);
		checkUpdateFrequency();
		checkNotification(false);
		latlng[0] = location.getLatitude();
		latlng[1] = location.getLongitude();
		com = new Intent("com.smallcrafts.wakemeup.update");
		com.putExtra("LatLng", latlng);
		com.putExtra("Distance", distance);
		LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(com);
		Log.d("DAEMON", "Location Update. Latitude: "+ Double.toString(latlng[0]) + " - Longitude: " + Double.toString(latlng[1]));
		
		//Check where if the alarm conditions are met
		float comparableDistance;
		
		if (units){
			comparableDistance = (float) ((float) distance*0.621371);
		} else {
			comparableDistance = distance;
		}
		
		Log.d("DAEMON", "Comparable Distance: " + Float.toString(comparableDistance));
		
		if ((comparableDistance < thresholdDistance) && !AlarmActivity.alarmVisible){
			launchAlarm();
			

			if(snooze){
				Log.d("DAEMON","SnoozeCounter : " + Integer.toString(snoozeCounter));
				Log.d("DAEMON","Current ThresholdDistance: " + Integer.toString(thresholdDistance));
				if (snoozeCounter < 3){
					snoozeCounter++;
					thresholdDistance = (int) thresholdDistance/2;
				} else {
					Log.d("DAEMON","Automatically dismissed after 3 tries");
					locationManager.removeUpdates(this);
					stopSelf();
				}
			}
		}
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
}
