package com.voody.icecast.player;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivityCircle extends Activity {
    Button buttonSearch, buttonAdd, buttonSettings;
    Bundle sendBundle = new Bundle();
    ImageView menuCircle;
    int menu_circle_size, menu_circle_number = 0, displaySmaller;
    int[] widget_coord = new int[2];
    Intent intent;
    //final static long UPDATE_THRESHOLD = 604800; // Seconds: one week
    //final static long UPDATE_THRESHOLD = 3600; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        // Accessibility check
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (am.isEnabled())
        	setContentView(R.layout.activity_main_buttons);
        else
        	setContentView(R.layout.activity_main_circle);
        
        if (!isOnline()) {
        	Toast toast = Toast.makeText(MainActivityCircle.this, getString(R.string.no_internet), Toast.LENGTH_SHORT);
        	toast.show();
        	finish();
        }
        
        buttonSearch = (Button)findViewById(R.id.search_button);
        buttonSearch.setOnClickListener(buttonSearchClickListener); 
        
        menuCircle = (ImageView)findViewById(R.id.menu_circle);
        menuCircle.setOnTouchListener(menuCircleTouchListener);
        
        buttonAdd = (Button)findViewById(R.id.manually_add);
        buttonAdd.setOnClickListener(buttonManuallyClickListener);
        
        buttonSettings = (Button)findViewById(R.id.settings);
        buttonSettings.setOnClickListener(buttonSettingsClickListener);
        
        SQLiteHelper dbHelper = new SQLiteHelper(MainActivityCircle.this);
        String auto_refresh = dbHelper.getSetting("auto_refresh");
        if (auto_refresh.equals("1")) {
        	long last_update = dbHelper.getUpdates();
        	long unix_timestamp = System.currentTimeMillis()/1000;
        	String refresh_days = dbHelper.getSetting("refresh_days");
        	long threshold = Integer.parseInt(refresh_days) * 86400;
        	if ((unix_timestamp - last_update) > threshold) {
        		Intent intent = new Intent(MainActivityCircle.this, DownloadFile.class);
        		startActivity(intent);
        	}
        }
        dbHelper.close();
    }
   
    public boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
    
    OnTouchListener menuCircleTouchListener = new OnTouchListener(){
        public boolean onTouch(View v, MotionEvent event){
        	if (event.getAction() == MotionEvent.ACTION_DOWN){	
        		
        		// Calc width here, because widget's width is not available in onCreate()
        		menu_circle_size = menuCircle.getWidth();       		
        		menu_circle_number = 0;
        	
        		// Get touch position
        		int absX = (int)event.getRawX();
        		int absY = (int)event.getRawY();

        		// Get widget position
        		v.getLocationOnScreen(widget_coord);
            
        		// Calc relative coordinates inside the image 
        		int relX = absX - widget_coord[0];
        		int relY = absY - widget_coord[1];
            
        		// Check if the image was touched
        		if ((relX > 0) && (relX < menu_circle_size) && (relY > 0) && (relY < menu_circle_size)) {
        			menu_circle_number = MenuCircle.getCircle(relX, relY, menu_circle_size);
        		}
        		
        		switch(menu_circle_number) {
            		case 1:
            			sendBundle.putString("mode", "favourites");
            			intent = new Intent(MainActivityCircle.this, StationListActivity.class);
            			intent.putExtras(sendBundle);
            			startActivity(intent);
            			return true;
            		case 2:
            			sendBundle.putString("mode", "recent");
            			intent = new Intent(MainActivityCircle.this, StationListActivity.class);
            			intent.putExtras(sendBundle);
            			startActivity(intent);
            			return true;
            		case 3:
            			intent = new Intent(MainActivityCircle.this, GenreListActivity.class);
            			startActivity(intent);
            			return true;
            		default:
            			return true;
        		}
        	}
        	return true;
        }
    };
        
    Button.OnClickListener buttonSearchClickListener = new Button.OnClickListener(){
       	public void onClick(View view)  {
       		EditText edit_text = (EditText) findViewById(R.id.search_form);
       		String query = edit_text.getText().toString();
       		sendBundle.putString("mode", "search");
       		sendBundle.putString("query", query);
	        Intent intent = new Intent(view.getContext(), StationListActivity.class);
	        intent.putExtras(sendBundle);
	        startActivity(intent);
       	}
    };
    
    Button.OnClickListener buttonManuallyClickListener = new Button.OnClickListener(){
       	public void onClick(View view)  {
       		Intent intent = new Intent(view.getContext(), ManuallyAddStation.class);
	        startActivity(intent);
       	}
    };
    
    Button.OnClickListener buttonSettingsClickListener = new Button.OnClickListener(){
       	public void onClick(View view)  {
       		Intent intent = new Intent(view.getContext(), Settings.class);
	        startActivity(intent);
       	}
    };
}
