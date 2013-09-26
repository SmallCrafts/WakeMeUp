package com.smallcrafts.wakemeup;

import java.util.ArrayList;

import com.smallcrafts.wakemeup.R;

import android.location.Address;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainMenu extends Activity {

	private Button locationButton;
	private Button settingsButton;
	private Button startButton;
	private TextView activeLocation;
	private boolean dialogResponse;
	private CustomAddress destinationAddress;
	private static final int LOCATION_SEARCH_REQUEST = 1;
	static final String SAVED_DESTINATION = "savedDestination";
	public static final String LOCATION_SEARCH_STRING = "com.smallcrafts.wakemeup.locationactivity.addressresult";
	public static boolean mmenuVisible = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainmenu);
		
		locationButton = (Button) findViewById(R.id.location);
		locationButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (LocationDaemon.isRunning() != null){
					Log.d("MAINMENU","Service running!! Launch Warning.");
					AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
					builder.setMessage(Html.fromHtml("<p align=center>"+ getString(R.string.mmwarningmessage1) + "</p><p align=center><b>" + destinationAddress.toString() + "</b></p><p align=center>"+ getString(R.string.mmwarningmessage2) + "</p>"))
				       .setTitle(R.string.mmwarning);
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               // User clicked OK button
								Log.d("MAINMENU","Current tracking dismissed. Launching LocationActivity.");
								destinationAddress = null;
								stopService(new Intent(MainMenu.this, LocationDaemon.class));
								launchLocation();
				           }
				       });
					builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               // User cancelled the dialog
				           }
				       });
					
					// Create the AlertDialog
					AlertDialog dialog = builder.create();
					dialog.show();

				} else {
					Log.d("MAINMENU","No service running. Launching LocationActivity.");
					launchLocation();
				}
			}
		});
		
		settingsButton = (Button) findViewById(R.id.settings);
		settingsButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				launchSettings();
			}
			
		});
		
		startButton = (Button) findViewById(R.id.start);
		startButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				launchService();
			}
			
		});
		
		activeLocation = (TextView) findViewById(R.id.active_location);
		activeLocation.setVisibility(TextView.GONE);
		
	}

	@Override 
	protected void onResume(){
		super.onResume();
		mmenuVisible = true;
		
		if (LocationDaemon.isRunning() != null){
			LocationDaemon.removeOSNotification();
			destinationAddress = LocationDaemon.isRunning();
			startButton.setText("Watch!");
		} else {
			startButton.setText("Go!");
		}
		
		if (destinationAddress != null){
			activeLocation.setText(destinationAddress.toString());
			activeLocation.setVisibility(TextView.VISIBLE);
			Log.d("MAINMENU", "Current Address: " + destinationAddress.toString());
		} else {
			activeLocation.setVisibility(TextView.GONE);
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		mmenuVisible = false;
	}
	
	@Override
	public void onStop(){
		super.onStop();
		mmenuVisible = false;
		if(LocationDaemon.isRunning() != null)
			LocationDaemon.launchOSNotification(this);
		Log.d("MAINMENU", "MainMenu Activity Stopped ... ... ... ... ");
	}
	
	private void launchLocation(){
		Intent locationIntent = new Intent(MainMenu.this, LocationActivity.class);
		startActivityForResult(locationIntent, LOCATION_SEARCH_REQUEST);
	}
	
	private void launchSettings(){
		Intent settingsIntent = new Intent(MainMenu.this, SettingsActivity.class);
		startActivity(settingsIntent);
	}
	
	private void launchService(){
		if (destinationAddress != null){
			Intent serviceIntent = new Intent(MainMenu.this, ServiceActivity.class);
			serviceIntent.putExtra("com.smallcrafts.wakemeup.destination", (Address)destinationAddress);
			startActivity(serviceIntent);	
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
			builder.setMessage(R.string.mmdialogmessage)
		       .setTitle(R.string.mmdialogtitle);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User clicked OK button
						launchLocation();
		           }
		       });
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User cancelled the dialog
		           }
		       });
			
			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data); 
		
		switch(requestCode) {
			case LOCATION_SEARCH_REQUEST : {
				if (resultCode == Activity.RESULT_OK) {
					destinationAddress = new CustomAddress((Address) data.getParcelableExtra(LOCATION_SEARCH_STRING));
					Log.d("MAINMENU", destinationAddress.toString());
				} 
			break; 
			} 
		} 
	}
	
	@Override
	public void onSaveInstanceState(Bundle sis){
		Log.d("MAINMENU","InstaceState Saved");
		sis.putParcelable(SAVED_DESTINATION, (Address)destinationAddress);
		super.onSaveInstanceState(sis);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle ris){
		Log.d("MAINMENU","InstaceState Restored");
		super.onRestoreInstanceState(ris);
		destinationAddress = new CustomAddress((Address)ris.getParcelable(SAVED_DESTINATION));
	}
}
