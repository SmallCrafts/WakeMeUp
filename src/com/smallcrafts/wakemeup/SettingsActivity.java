package com.smallcrafts.wakemeup;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends Activity {
	
	private static SeekBar distanceBar;
	private static Switch vibratorSwitch;
	private static Switch soundSwitch;
	private static Switch saveTripSwitch;
	private static Switch snoozeSwitch;
	private static Switch unitsSwitch;
	private static TextView textDistance;
	private static SharedPreferences sharedPref;
	public static boolean settingsVisible = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		setupActionBar();
		
		sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		
		textDistance = (TextView) findViewById(R.id.distance_text);
		
		distanceBar = (SeekBar) findViewById(R.id.distance_bar);
		distanceBar.setMax(10);
		distanceBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar sb, int v, boolean u) {
				if (v == 0){
					textDistance.setText(Float.toString(0.5f));
				} else {
					textDistance.setText(Integer.toString(v));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				float value;
				try{
					value = Float.parseFloat(textDistance.getText().toString());
				} catch (NumberFormatException e){
					value = 5;
				}

				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putFloat("distance", value);
				Boolean bol = editor.commit();
				
				if (snoozeSwitch.isChecked() && seekBar.getProgress() < 2){
					snoozeWarning();
				}
				
				Log.d("DISTANCE", Double.toString(value) + " Commited? " + bol.toString());
				
			}
		});
		
		
		vibratorSwitch = (Switch) findViewById(R.id.notify_vibrator);
		vibratorSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton sw, boolean s) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("vibrator", s);
				Boolean bol = editor.commit();
				Log.d("VIBRATOR", Boolean.toString(s) + " Commited? " + bol.toString());
			}
		});
		
		soundSwitch = (Switch) findViewById(R.id.notify_sound);
		soundSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton sw, boolean s) {				
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("sound", s);
				Boolean bol = editor.commit();
				Log.d("SOUND", Boolean.toString(s) + " Commited? " + bol.toString());
			}
		});
		
		saveTripSwitch = (Switch) findViewById(R.id.save_last_trip);
		saveTripSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton sw, boolean s) {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("savetrip", s);
				Boolean bol = editor.commit();
				Log.d("SAVETRIP", Boolean.toString(s) + " Commited? " + bol.toString());
			}
		});
		
		snoozeSwitch = (Switch) findViewById(R.id.snooze);
		snoozeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton sw, boolean s) {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("snooze", s);
				Boolean bol = editor.commit();
				Log.d("SNOOZE", Boolean.toString(s) + " Commited? " + bol.toString());
				
				if (s && distanceBar.getProgress() < 2){
					snoozeWarning();
				}
			}
		});

		unitsSwitch = (Switch) findViewById(R.id.units);
		unitsSwitch.setTextOff(getString(R.string.kilometers));
		unitsSwitch.setTextOn(getString(R.string.miles));
		unitsSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton sw, boolean s) {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("units", s);
				Boolean bol = editor.commit();
				Log.d("UNITS_MILES", Boolean.toString(s) + " Commited? " + bol.toString());
			}
		});		
		
		restoreSettings();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		settingsVisible = true;
		if(LocationDaemon.isRunning() != null)
			LocationDaemon.removeOSNotification();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		settingsVisible = false;
		if(LocationDaemon.isRunning() != null)
			LocationDaemon.launchOSNotification(this);
		Log.d("SETTINGS", "Settings Activity Stopped ... ... ... ... ");
	}
	
	private void snoozeWarning(){
		Toast.makeText(getBaseContext(), getResources().getString(R.string.snoozeWarning), Toast.LENGTH_LONG).show();
	}
	
	private void restoreSettings(){
		sharedPref = this.getSharedPreferences(getString(R.string.cpref), Context.MODE_PRIVATE);
		float value = sharedPref.getFloat("distance", 5.0f);
		if (value == 0.5f){
			Log.d("SETTINGS", "Threshold MIN Value: " + Float.toString(value));
			distanceBar.setProgress(0);
			textDistance.setText(Float.toString(0.5f));
		} else {
			Log.d("SETTINGS", "Threshold Value: " + Float.toString(value));
			distanceBar.setProgress(Math.round(value));
		}
		vibratorSwitch.setChecked(sharedPref.getBoolean("vibrator", true));
		soundSwitch.setChecked(sharedPref.getBoolean("sound", true));
		saveTripSwitch.setChecked(sharedPref.getBoolean("savetrip", false));
		snoozeSwitch.setChecked(sharedPref.getBoolean("snooze", false));
		unitsSwitch.setChecked(sharedPref.getBoolean("units", false));
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.settings, menu);
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

}
