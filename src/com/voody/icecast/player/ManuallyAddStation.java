package com.voody.icecast.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ManuallyAddStation extends Activity {
	ImageView buttonFavourite, buttonHome, buttonPlay;
	TextView textViewServerName, textViewListenUrl, textViewBitrate;
	String server_name, listen_url, bitrate, rowid;
	Boolean isFavourite = false;
	Boolean editStation = false;
		
	SQLiteHelper dbHelper = new SQLiteHelper(ManuallyAddStation.this);
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_url);
		    
        buttonFavourite = (ImageView)findViewById(R.id.favourite);
        buttonFavourite.setOnTouchListener(buttonFavouriteTouchListener);
		
        buttonHome = (ImageView)findViewById(R.id.go_home);
        buttonHome.setOnTouchListener(buttonHomeTouchListener);

		buttonPlay = (ImageView)findViewById(R.id.play);
		buttonPlay.setOnTouchListener(buttonPlayTouchListener);

        textViewServerName = (TextView) findViewById(R.id.editText_server_name_2);
                
        textViewListenUrl = (TextView) findViewById(R.id.editText_listen_url_2);

		// Check if we need to edit a station
		Bundle editBundle = this.getIntent().getExtras();
		if (editBundle != null) {
			editStation = true;
			server_name = editBundle.getString("server_name");
			listen_url = editBundle.getString("listen_url");
			bitrate = editBundle.getString("bitrate");
			rowid = editBundle.getString("rowid");

			textViewListenUrl.setText(listen_url);
			textViewServerName.setText(server_name);

			buttonFavourite.setImageResource(R.drawable.b6);
			buttonFavourite.setContentDescription(getString(R.string.acc_save));

			setTitle(getResources().getText(R.string.title_manually_edit));

			if (dbHelper.isFavourite(listen_url))
				isFavourite = true;
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		dbHelper.close();
	}
	
	public void onStop() {
		super.onStop();
	}	
	
	Button.OnTouchListener buttonFavouriteTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		Toast toast = null;
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			server_name = textViewServerName.getText().toString();
	   			listen_url = textViewListenUrl.getText().toString();

				// Edit a station or insert a new favourite?
				if (editStation) {
					toast = Toast.makeText(ManuallyAddStation.this, getString(R.string.edited_ok), Toast.LENGTH_SHORT);
					// Favourite or not?
					if (isFavourite)
						dbHelper.editFavourite(server_name, listen_url, rowid);
					else
						dbHelper.editStation(server_name, listen_url, rowid);
				}
				else {
					if (dbHelper.isFavourite(listen_url))
						isFavourite = true;

					if (isFavourite) {
						buttonFavourite.setImageResource(R.drawable.b3_add);
						buttonFavourite.setContentDescription(getString(R.string.cd_favourite_add));
						toast = Toast.makeText(ManuallyAddStation.this, getString(R.string.manually_exists), Toast.LENGTH_SHORT);
					}
					else {
						dbHelper.insertManuallyStation(server_name, listen_url);
						buttonFavourite.setImageResource(R.drawable.b3_del);
						buttonFavourite.setContentDescription(getString(R.string.cd_favourite_del));
						toast = Toast.makeText(ManuallyAddStation.this, getString(R.string.manually_added), Toast.LENGTH_SHORT);
					}
				}

				if (toast != null)
   	        		toast.show();
	   		}
	   		return true;
	   	}
	};
	
	Button.OnTouchListener buttonHomeTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			Intent intent = new Intent(ManuallyAddStation.this, MainActivityCircle.class);
	   			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	   			startActivity(intent);
	   		}
	        return true;
	   	}
	};

	Button.OnTouchListener buttonPlayTouchListener = new Button.OnTouchListener(){
		public boolean onTouch(View view, MotionEvent event)  {
			Bundle sendBundle = new Bundle();

			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				sendBundle.putString("server_name", server_name);
				sendBundle.putString("listen_url", listen_url);
				sendBundle.putString("bitrate", bitrate);
				sendBundle.putString("rowid", rowid);

				Intent intent = new Intent(ManuallyAddStation.this, StationListenActivityImg.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtras(sendBundle);

				startActivity(intent);
			}
			return true;
		}
	};
} 
