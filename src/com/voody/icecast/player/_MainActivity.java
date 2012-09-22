package com.voody.icecast.player;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class _MainActivity extends Activity {
    Button buttonInit;
    Button buttonGenres;
    Button buttonRecent;
    Button buttonFavourites;
    Button buttonSearch;
    Bundle sendBundle = new Bundle();
    //final static long UPDATE_THRESHOLD = 604800; // Seconds: one week
    final static long UPDATE_THRESHOLD = 3600; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._activity_main);
        
        buttonInit = (Button)findViewById(R.id.init);
        buttonInit.setOnClickListener(buttonInitClickListener);

        buttonGenres = (Button)findViewById(R.id.genres);
        buttonGenres.setOnClickListener(buttonGenresClickListener);

        buttonRecent = (Button)findViewById(R.id.recent);
        buttonRecent.setOnClickListener(buttonRecentClickListener);
        
        buttonFavourites = (Button)findViewById(R.id.favourites);
        buttonFavourites.setOnClickListener(buttonFavouritesClickListener);

        buttonSearch = (Button)findViewById(R.id.search_button);
        buttonSearch.setOnClickListener(buttonSearchClickListener);
        
        SQLiteHelper dbHelper = new SQLiteHelper(_MainActivity.this);
        long last_update = dbHelper.getUpdates();
        long unix_timestamp = System.currentTimeMillis()/1000;
        if ((unix_timestamp - last_update) > UPDATE_THRESHOLD) {
	        Intent intent = new Intent(_MainActivity.this, DownloadFile.class);
	        startActivity(intent);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    Button.OnClickListener buttonInitClickListener = new Button.OnClickListener(){
       	public void onClick(View view)  {
	        Intent intent = new Intent(view.getContext(), DownloadFile.class);
	        startActivity(intent);
       	}
    };
    
    Button.OnClickListener buttonGenresClickListener = new Button.OnClickListener(){
       	public void onClick(View view)  {
	        Intent intent = new Intent(view.getContext(), GenreListActivity.class);
	        startActivity(intent);
       	}
    };  
    
    Button.OnClickListener buttonRecentClickListener = new Button.OnClickListener(){
       	public void onClick(View view)  {
       		sendBundle.putString("mode", "recent");
	        Intent intent = new Intent(view.getContext(), StationListActivity.class);
	        intent.putExtras(sendBundle);
	        startActivity(intent);
       	}
    };

    Button.OnClickListener buttonFavouritesClickListener = new Button.OnClickListener(){
       	public void onClick(View view)  {
       		sendBundle.putString("mode", "favourites");
	        Intent intent = new Intent(view.getContext(), StationListActivity.class);
	        intent.putExtras(sendBundle);
	        startActivity(intent);
       	}
    };

    Button.OnClickListener buttonSearchClickListener = new Button.OnClickListener(){
       	public void onClick(View view)  {
       		EditText edit_text = (EditText) findViewById(R.id.search_form);
       		String query = edit_text.toString();
       		sendBundle.putString("mode", "search");
       		sendBundle.putString("query", query);
	        Intent intent = new Intent(view.getContext(), StationListActivity.class);
	        intent.putExtras(sendBundle);
	        startActivity(intent);
       	}
    };
}
