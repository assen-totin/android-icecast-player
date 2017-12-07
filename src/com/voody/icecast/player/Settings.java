package com.voody.icecast.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

public class Settings extends Activity {
	ImageView buttonHome;
	Button buttonReload;
	CheckBox cbAutoRefresh;
	EditText textViewDays;
	String auto_refresh, refresh_days;
	Boolean auto_enabled = false;
		
	SQLiteHelper dbHelper = new SQLiteHelper(Settings.this);
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		    	
        buttonHome = (ImageView)findViewById(R.id.go_home);
        buttonHome.setOnTouchListener(buttonHomeTouchListener);
        
        cbAutoRefresh = (CheckBox) findViewById(R.id.settings_cb);
        cbAutoRefresh.setOnClickListener(cbAutoRefreshClickListener);
        
        textViewDays = (EditText) findViewById(R.id.settings_refresh_text);
        textViewDays.addTextChangedListener(tw);

		buttonReload = (Button) findViewById(R.id.reload_button);
		buttonReload.setOnTouchListener(buttonReloadTouchListener);

        String auto_refresh = dbHelper.getSetting("auto_refresh");
        String refresh_days = dbHelper.getSetting("refresh_days");
        
        textViewDays.setText(refresh_days);
        if (auto_refresh.equals("1")) {
        	cbAutoRefresh.setChecked(true);
        	//textViewDays.setEnabled(true);
        	//textViewDays.setFocusable(true);
        }
        else {
        	cbAutoRefresh.setChecked(false);
        	textViewDays.setEnabled(false);  
        	textViewDays.setFocusable(false);
        }
	}
	
	public void onDestroy() {
		super.onDestroy();
		dbHelper.close();
	}
	
	public void onStop() {
		super.onStop();
	}	

	TextWatcher tw = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable arg0) {
			String refresh_days = arg0.toString();
			dbHelper.setSetting("refresh_days", refresh_days);
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {		
		}
	};
	
	CheckBox.OnClickListener cbAutoRefreshClickListener = new CheckBox.OnClickListener(){
		@Override
		public void onClick(View cb) {
			refresh_days = textViewDays.getText().toString();
			if (((CheckBox) cb).isChecked()) {
				textViewDays.setFocusableInTouchMode(true);
				textViewDays.setEnabled(true);
				dbHelper.setSetting("auto_refresh", "1");
				dbHelper.setSetting("refresh_days", refresh_days);
			}
			else {
				textViewDays.setEnabled(false);
				textViewDays.setFocusable(false);
				dbHelper.setSetting("auto_refresh", "0");
			}
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

	Button.OnTouchListener buttonReloadTouchListener = new Button.OnTouchListener(){
		public boolean onTouch(View view, MotionEvent event)  {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				Intent intent = new Intent(Settings.this, DownloadFile.class);
				startActivity(intent);
			}
			return true;
		}
	};
} 
