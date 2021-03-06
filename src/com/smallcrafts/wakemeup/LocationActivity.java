package com.smallcrafts.wakemeup;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.smallcrafts.wakemeup.R;
import com.smallcrafts.wakemeup.R.layout;
import com.smallcrafts.wakemeup.R.menu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;

import android.support.v4.app.NavUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationActivity extends Activity {

	private static AutoCompleteTextView input;
	private static GoogleMap map;
	private static UiSettings mSettings;
	private static ArrayList<Marker> RECENT = null;
	private static Map<String, LatLng> SUGGESTIONS = null;
	private static CustomArrayAdapter<CustomAddress> adapter;
	private static CustomAddress searchAddress;
	private static CustomAddress destinationAddress;
	private static Marker destinationMarker = null;
	private static SharedPreferences sharedPref;
	private static String searchAddressText;
	private static Location myLocation;
	private static Button doneButton;
	private static Vibrator vibrator;
	private static int thresholdDistance;
	private static boolean saveLastTrip;
	private static boolean vib;
	private static boolean sound;
	private static boolean snooze;
	private static boolean units;
	public static boolean locationVisible = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		restoreSettings();
		RECENT = new ArrayList<Marker>();
		
		Log.d("MAP", "Map created");
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.setMyLocationEnabled(true);
		map.setOnMapLongClickListener(new OnMapLongClickListener(){
			@Override
			public void onMapLongClick(LatLng point) {
				vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(100);
				setMarker(point);
				
			}
			
		});

		map.setOnMarkerClickListener(new OnMarkerClickListener(){

			@Override
			public boolean onMarkerClick(Marker marker) {				
				map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
				// Check if the clicked marker is NOT the destinationMarker
				if (!marker.equals(destinationMarker)){
					LatLng tmp;
					
					tmp = marker.getPosition();
					setMarker(tmp);
					marker.remove();
					RECENT.remove(marker);
					RECENT.add(destinationMarker);
					
					return true;
				} else {
					return false;
				}
			}
			
		});
		
		myLocation = getCurrentLocation();
		
		if (myLocation != null){
			Log.d("MYLOCATION", Double.toString(myLocation.getLatitude()) + " --- " + Double.toString(myLocation.getLongitude()));
			// Center de camera to my location
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),30));
	  	    // Zoom in, animating the camera.
		    map.animateCamera(CameraUpdateFactory.zoomTo(10), 1500, null);
		}
		
		getRecent();
	    
		
	    doneButton = (Button) findViewById(R.id.done_button);
	    doneButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (destinationAddress != null){
					Intent i = new Intent();
					i.putExtra(MainMenu.LOCATION_SEARCH_STRING, (Address)destinationAddress);
					setResult(Activity.RESULT_OK, i);
					
					Log.d("LOCATION", "Save Last Trip : " + Boolean.toString(saveLastTrip));
					
					if (saveLastTrip){
						setRecent(destinationAddress.getLatitude(), destinationAddress.getLongitude());
					}
					finish();
				}		
			}
	    });
	    
		adapter = new CustomArrayAdapter<CustomAddress>(this, android.R.layout.simple_dropdown_item_1line);
		adapter.setNotifyOnChange(true);
		input = (AutoCompleteTextView) findViewById(R.id.input);
		input.setThreshold(3);
		input.setAdapter(adapter);
		input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		input.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				searchAddress = (CustomAddress) arg0.getItemAtPosition(arg2);
				Log.d("ITEMSELECTED", searchAddress.toString());
				searchAddressText = input.getText().toString();
				Log.d("ITEMSELECTED", searchAddressText);
			}
		});
			
		input.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent k) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		            
		            if (searchAddress != null && searchAddressText.equals(input.getText().toString())){
		            	destinationAddress = searchAddress;
		            	setMarker(destinationAddress);
		            	input.dismissDropDown();
		            	map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(destinationAddress.getLatitude(), destinationAddress.getLongitude()),15));
		            } else {
			            List<CustomAddress> list = reverseGeocoding(input.getText().toString());
		            	if (list != null){
		            		destinationAddress = list.get(0);
			            	setMarker(destinationAddress);
			            	input.dismissDropDown();
		            		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(destinationAddress.getLatitude(), destinationAddress.getLongitude()),15));
		            	} else {
		            		Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
		            	}
		            }
		            
		            return true;
		        }
		        return false;
			}
		});
		
		input.addTextChangedListener(new TextWatcher(){

			private String text = input.getText().toString();
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				text = input.getText().toString();
				
				if (s.length() > 1 && text.length() >= 3){
					Log.d("OTC","Conditions Met: " + s.toString() + " // Change: " + Integer.toString(start) + " // " + Integer.toString(before) + " // " + Integer.toString(count));
					new AsyncTask<String, Void, List<CustomAddress>>(){

						@Override
						protected List<CustomAddress> doInBackground(String... arg0) {
							List<CustomAddress> addresses = reverseGeocoding(arg0[0]);
							Log.d("BGDPROC","Start");
//							List<String> list = null;
//							if (addresses != null){
//								Log.d("BGDPROC","List is not NULL : " + Integer.toString(addresses.size()));
//								list = new ArrayList<String>();
//								for (Address a : addresses){
//									list.add(formatAddress(a));
//									Log.d("REVGEOCODING", formatAddress(a));
//								}
//							}

							return addresses;
						}
						
						@Override
						protected void onPostExecute(List<CustomAddress> list){
							Log.d("BGDPROC","PostExecute");

							if (list != null){
								for (CustomAddress s: list){
									Log.d("BGDPROC","Element added: " + s.toString());
								}
				
								adapter.clear();
								adapter.addAll(list);
								adapter.notifyDataSetChanged();
								
								Log.d("BGDPROC","Adapter Size: " + Integer.toString(adapter.getCount()));
								
								for (int i=0; i < adapter.getCount(); i++){
									Log.d("BGDPROC","Item " + Integer.toString(i) + adapter.getItem(i).toString());
								}
							} else{
								Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
							}
						}
						
					}.execute(text);
				}
			}
		});
		
		mSettings = map.getUiSettings();
		mSettings.setMyLocationButtonEnabled(true);
	}

	@Override
	protected void onResume(){
		super.onResume();
		locationVisible = true;
	}
	
	@Override
	public void onPause(){
		super.onPause();
		locationVisible = false;
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.location, menu);
		return false;
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private int isRecentDestination(){

		for (Marker m: RECENT){
			if (m.equals(destinationMarker))
				return RECENT.indexOf(m);
		}
		
		return -1 ;
	}
	
	private Marker closeMarkers(Marker ref){
		Marker closest = null;
		if (RECENT != null){
			float distance = 500;
			float[] results = new float[1];
			for (Marker m: RECENT){
				Location.distanceBetween(ref.getPosition().latitude, ref.getPosition().longitude, m.getPosition().latitude, m.getPosition().longitude, results);
				if (results[0] < distance){
					distance = results[0];
					closest = m;
				}
			}
		}
		return closest;
	}
	
	private Boolean setRecent(double lat, double lon){
		SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Map<String, ?> recentMap = sharedPref.getAll();
		Set<String> key = recentMap.keySet();
		int index = 0;
		
		LatLng rec = new LatLng(lat, lon);
		
		if (SUGGESTIONS.containsValue(rec)){
			for (String s: SUGGESTIONS.keySet()){
				if (SUGGESTIONS.get(s).equals(rec)){
					
				}
			}
		} else {
			index = SUGGESTIONS.size();
			
			for (int i = 0; i < index; i++ ){
				// Move all the locations 1 position backwards on the list
				editor.putLong("latitude" + Integer.toString(i+1), Double.doubleToLongBits(SUGGESTIONS.get("recent" + Integer.toString(i)).latitude));
				editor.putLong("longitude" + Integer.toString(i+1), Double.doubleToLongBits(SUGGESTIONS.get("recent" + Integer.toString(i)).longitude));
			}
			
			//Last location to recent list
			editor.putLong("latitude0", Double.doubleToLongBits(lat));
			editor.putLong("longitude0", Double.doubleToLongBits(lon));
		}

		Boolean bol = editor.commit();
		return bol;
	}
	
	private Boolean getRecent(){
		SUGGESTIONS = new HashMap<String, LatLng>();
		SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		Set<String> key = sharedPref.getAll().keySet();
		for (int i = 0; i < 5; i++){
			if(key.contains("latitude" + Integer.toString(i))){
				Log.d("LOCATION", "Recent #" + Integer.toString(i) + " : Latitude : " + Double.toString(Double.longBitsToDouble(sharedPref.getLong("latitude" + Integer.toString(i), 0))));
				Log.d("LOCATION", "Recent #" + Integer.toString(i) + " : Longitude : " + Double.toString(Double.longBitsToDouble(sharedPref.getLong("longitude" + Integer.toString(i), 0))));
				LatLng t = new LatLng(Double.longBitsToDouble(sharedPref.getLong("latitude" + Integer.toString(i), 0)),Double.longBitsToDouble(sharedPref.getLong("longitude" + Integer.toString(i), 0)));
				SUGGESTIONS.put("recent" + Integer.toString(i), t);
				setRecentMarker(t);
			}
		}
		return true;
	}
	

	
	private Marker setRecentMarker(LatLng l){
		
		Marker t = map.addMarker(new MarkerOptions()
    	.position(l)
    	.title("Yes! This is where I'm going!")
    	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
	
		List<CustomAddress> destinations = geocoding(l.latitude, l.longitude);
		
		if (destinations != null){
			t.setSnippet(destinations.get(0).toString());
		}
		RECENT.add(t);
		return t;
	}
	
	private Marker setMarker(CustomAddress a){
		destinationMarker = setMarker(new LatLng(a.getLatitude(), a.getLongitude()));
		destinationMarker.setSnippet(a.toString());
		return destinationMarker;
	}
	
	private Marker setMarker(LatLng l){
		
		if (destinationMarker != null){
			destinationMarker.remove();
			
			LatLng tmp;
			int index = isRecentDestination();
			if (index >= 0){
				tmp = destinationMarker.getPosition();
				RECENT.remove(destinationMarker);
				setRecentMarker(tmp);
			}
		}

		destinationMarker = map.addMarker(new MarkerOptions()
        	.position(l)
        	.title("Yes! This is where I'm going!")
        	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
		
		List<CustomAddress> destinations = geocoding(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude);
		
		if (destinations != null){
			destinationMarker.setSnippet(destinations.get(0).toString());
			destinationAddress = destinations.get(0);
		}
		
		destinationMarker.showInfoWindow();
		
		return destinationMarker;
	}
	
	private List<CustomAddress> geocoding(double lat, double lon){
		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		try{
			List<Address> addresses = geoCoder.getFromLocation(lat, lon, 3);
			List<CustomAddress> ca = new ArrayList<CustomAddress>();
			if (!addresses.isEmpty()){
				for (Address a: addresses){
					ca.add(new CustomAddress(a));
				}
				return ca;
			} else {
				return null;
			}
			
		} catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	private List<CustomAddress> reverseGeocoding(String l){
		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocationName(l, 5);
			List<CustomAddress> ca = new ArrayList<CustomAddress>();
			
			String strCompleteAddress = "";
			if (addresses.size() > 0) {
				for (Address a : addresses){
					CustomAddress c = new CustomAddress(a);
					ca.add(c);
					strCompleteAddress = a.toString();
					Log.d("REVGEOCODING", strCompleteAddress);
				}
				return ca;
			}
			else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Location getCurrentLocation(){
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		Log.d("LOCATION", "'Best' location provider" + locationManager.getBestProvider(new Criteria(), true));
		Location bestLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
		Location currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
		if (bestLocation != null){
			Log.d("LOCATION", "'Best' location used. Provider: " + locationManager.getBestProvider(new Criteria(), true));
			return bestLocation;
		}
		else{
			Log.d("LOCATION", "Last Network Location used.");
			return currentLocation;
		}
	}
	
	private void restoreSettings(){
		sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		thresholdDistance =(int)(sharedPref.getFloat("distance", 5)*1000.0);
		Log.d("SERVICE", "Thershold: " + Integer.toString(thresholdDistance));
		vib = sharedPref.getBoolean("vibrator", true);
		sound = sharedPref.getBoolean("sound", true);
		snooze = sharedPref.getBoolean("snooze", false);
		units = sharedPref.getBoolean("units", true);
		saveLastTrip = sharedPref.getBoolean("savetrip", false);
	}
}
