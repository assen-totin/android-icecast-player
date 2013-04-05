package com.voody.icecast.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity {
	ImageView buttonFavourite, buttonHome;
	CheckBox cbAutoRefresh;
	TextView textViewDays;
	String auto_refresh, refresh_days;
	Boolean auto_enabled = false;
		
	SQLiteHelper dbHelper = new SQLiteHelper(Settings.this);
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		    
        buttonFavourite = (ImageView)findViewById(R.id.favourite);
        buttonFavourite.setOnTouchListener(buttonFavouriteTouchListener);
		
        buttonHome = (ImageView)findViewById(R.id.go_home);
        buttonHome.setOnTouchListener(buttonHomeTouchListener);
        
        cbAutoRefresh = (CheckBox) findViewById(R.id.settings_cb);
        
        textViewDays = (TextView) findViewById(R.id.settings_refresh_text);
        
        String auto_refresh = dbHelper.getSetting("auto_refresh");
        String refresh_days = dbHelper.getSetting("refresh_days");
        
        textViewDays.setText(refresh_days);
        if (auto_refresh.equals("1")) {
        	cbAutoRefresh.setChecked(true);
        	textViewDays.setEnabled(true);
        }
        else {
        	cbAutoRefresh.setChecked(false);
        	textViewDays.setEnabled(false);       	
        }
	}
	
	public void onDestroy() {
		super.onDestroy();
		dbHelper.close();
	}
	
	public void onStop() {
		super.onStop();
	}	

	CheckBox.OnClickListener cbAutoRefreshClickListener = new CheckBox.OnClickListener(){
		@Override
		public void onClick(View cb) {
			if (((CheckBox) cb).isChecked()) {
				textViewDays.setEnabled(true);
			}
			else {
				textViewDays.setEnabled(false);
			}
			
		}
	};
	
	Button.OnTouchListener buttonFavouriteTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		Toast toast;
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			auto_enabled = cbAutoRefresh.isChecked();
	   			refresh_days = textViewDays.getText().toString();
	   				   			   			
   				if (auto_enabled) {
   					dbHelper.setSetting("auto_refresh", "1");
   					dbHelper.setSetting("refresh_days", refresh_days);
   				}
   				else {
   					dbHelper.setSetting("auto_refresh", "0");
   					dbHelper.setSetting("refresh_days", refresh_days);
   				}
   				
   	        	toast = Toast.makeText(Settings.this, getString(R.string.settings_updated), Toast.LENGTH_SHORT);
   	        	toast.show();
	   		}
	   		return true;
	   	}
	};
	
	Button.OnTouchListener buttonHomeTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			Intent intent = new Intent(Settings.this, MainActivityCircle.class);
	   			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	   			startActivity(intent);
	   		}
	        return true;
	   	}
	};
} 
