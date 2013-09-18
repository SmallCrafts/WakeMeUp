package com.smallcrafts.wakemeup;

import java.util.ArrayList;

import com.smallcrafts.wakemeup.R;

import android.location.Address;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends Activity {

	private Button locationButton;
	private Button settingsButton;
	private Button startButton;
	private boolean dialogResponse;
	private CustomAddress destinationAddress;
	private static final int LOCATION_SEARCH_REQUEST = 1;
	public static final String LOCATION_SEARCH_STRING = "com.smallcrafts.wakemeup.locationactivity.addressresult";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainmenu);
		
		locationButton = (Button) findViewById(R.id.location);
		locationButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.d("MAINMENU","Almost there ... ");
				launchLocation();
				
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
			launchDialog();
		}
	}
	
	
	private boolean launchDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.mmdialogmessage)
	       .setTitle(R.string.mmdialogtitle);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               // User clicked OK button
	        	   dialogResponse = true;
	           }
	       });
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               // User cancelled the dialog
	        	   dialogResponse = false;
	           }
	       });
		
		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
		
		return dialogResponse;
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
					Log.d("MENU_ACTIVITY", destinationAddress.toString());
				} 
			break; 
			} 
		} 
	}
	
}
