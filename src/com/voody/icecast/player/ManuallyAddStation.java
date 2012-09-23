package com.voody.icecast.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ManuallyAddStation extends Activity {
	ImageView buttonFavourite, buttonHome;
	TextView textViewServerName, textViewListenUrl, textViewBitrate;
	String server_name, listen_url, bitrate;
	Boolean is_favourite = false;
		
	SQLiteHelper dbHelper = new SQLiteHelper(ManuallyAddStation.this);
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_url);
		    
        buttonFavourite = (ImageView)findViewById(R.id.favourite);
        buttonFavourite.setOnTouchListener(buttonFavouriteTouchListener);
		
        buttonHome = (ImageView)findViewById(R.id.go_home);
        buttonHome.setOnTouchListener(buttonHomeTouchListener);
        
        textViewServerName = (TextView) findViewById(R.id.editText_server_name_2);
                
        textViewListenUrl = (TextView) findViewById(R.id.editText_listen_url_2);
                
        textViewBitrate = (TextView) findViewById(R.id.editText_bitrate_2);
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
	   		Toast toast;
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			server_name = textViewServerName.getText().toString();
	   			listen_url = textViewListenUrl.getText().toString();
	   			bitrate = textViewBitrate.getText().toString();
	   				   			
	   	        if (dbHelper.isFavourite(listen_url)) {
	   	        	is_favourite = true;
	   	        }
	   			
	   			if (is_favourite) {
	   				toast = Toast.makeText(ManuallyAddStation.this, getString(R.string.manually_exists), Toast.LENGTH_SHORT);
	   			}
	   			else {
	   				dbHelper.insertManuallyStation(server_name, listen_url, bitrate);
	   	        	toast = Toast.makeText(ManuallyAddStation.this, getString(R.string.manually_added), Toast.LENGTH_SHORT);
	   			}
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
} 
