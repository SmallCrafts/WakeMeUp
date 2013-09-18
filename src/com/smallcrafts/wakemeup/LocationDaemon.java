package com.smallcrafts.wakemeup;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LocationDaemon extends Service implements LocationListener {

	private static LocationManager locationManager;
	private static Criteria criteria;
	private static Intent com;
	private static CustomAddress destinationAddress = null;
	// 0. location latitude , 1. location longitude
	private static double[] latlng = new double[2];
	private static float[] results = new float[1];
	private static float distance;
	private static float lastDistance;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	    criteria.setAltitudeRequired(false);
	    criteria.setBearingRequired(false);
	    criteria.setCostAllowed(false);
	    criteria.setSpeedRequired(false);
	    criteria.setPowerRequirement(Criteria.POWER_LOW);
		locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), 5, 0, this);
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
		locationManager.removeUpdates(this);
	}

	private void calculateDistance(Location l){
		if (destinationAddress != null){
			try{
				Location.distanceBetween(l.getLatitude(), l.getLongitude(), destinationAddress.getLatitude(), destinationAddress.getLongitude(), results);
				distance = results[0];
			} catch (IllegalArgumentException e) {
				
			}
		}
	}
	
	private void checkNotification(){

	}
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		latlng[0] = location.getLatitude();
		latlng[1] = location.getLongitude();
		com = new Intent("com.smallcrafts.wakemeup.update");
		com.putExtra("LatLng", latlng);
		boolean status = LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(com);
		Log.d("DAEMON", "Location Update. Latitude: "+ Double.toString(latlng[0]) + " - Longitude: " + Double.toString(latlng[1]));
		Log.d("DAEMON", "Broadcaste Status: " + Boolean.toString(status));
		calculateDistance(location);
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
