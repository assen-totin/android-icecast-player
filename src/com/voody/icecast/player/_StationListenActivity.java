package com.voody.icecast.player;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class _StationListenActivity extends Activity {
	private MediaPlayer mediaPlayer = new MediaPlayer();
	Button buttonStop, buttonPlay, buttonFavourite, buttonHome;
	TextView textViewServerName, textViewListenUrl, textViewBitrate;
	String server_name, listen_url, bitrate;
	Boolean is_favourite = false;
	Bundle recvBundle;
	
	SQLiteHelper dbHelper = new SQLiteHelper(_StationListenActivity.this);
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout._listen_url);
		
		recvBundle = this.getIntent().getExtras();
		if (recvBundle == null)
			recvBundle = savedInstanceState;
		
    	server_name = recvBundle.getString("server_name");
    	listen_url = recvBundle.getString("listen_url");
    	bitrate = recvBundle.getString("bitrate");
		 	
		dbHelper.insertIntoRecent(listen_url);

        buttonStop = (Button)findViewById(R.id.stop);
        buttonStop.setOnClickListener(buttonStopClickListener);
        buttonStop.setEnabled(false);
		
        buttonPlay = (Button)findViewById(R.id.play);
        buttonPlay.setOnClickListener(buttonPlayClickListener);
        buttonPlay.setEnabled(false);
        
        buttonFavourite = (Button)findViewById(R.id.favourite);
        buttonFavourite.setOnClickListener(buttonFavouriteClickListener);
		
        buttonHome = (Button)findViewById(R.id.go_home);
        buttonHome.setOnClickListener(buttonHomeClickListener);
        
        textViewServerName = (TextView) findViewById(R.id.textView_server_name_2);
        textViewServerName.setText(server_name);
        
        textViewListenUrl = (TextView) findViewById(R.id.textView_listen_url_2);
        textViewListenUrl.setText(listen_url);
        
        textViewBitrate = (TextView) findViewById(R.id.textView_bitrate_2);
        textViewBitrate.setText(bitrate + " kbps");
        
        if (dbHelper.isFavourite(listen_url)) {
        	is_favourite = true;
        	buttonFavourite.setText(R.string.favourite_del);
        }
        
		mediaPlayer.reset();
		try {
			mediaPlayer.setDataSource(listen_url);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			mediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} 
		
		mediaPlayer.start();
		
		buttonStop.setEnabled(true);
	}
	
	public void onDestroy() {
		super.onDestroy();
		mediaPlayer.stop();
		mediaPlayer.release();
		dbHelper.close();
	}
	
	public void onStop() {
		super.onStop();
		mediaPlayer.stop();
	}	
	
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("server_name", server_name);
		savedInstanceState.putString("listen_url", listen_url);
		savedInstanceState.putString("bitrate", bitrate);
	}
	
	Button.OnClickListener buttonStopClickListener = new Button.OnClickListener(){
	   	public void onClick(View view)  {
	       	mediaPlayer.pause();
	       	buttonStop.setEnabled(false);
	       	buttonPlay.setEnabled(true);
	   	}
	};
		
	Button.OnClickListener buttonPlayClickListener = new Button.OnClickListener(){
	   	public void onClick(View view)  {
	       	mediaPlayer.start();
	       	buttonStop.setEnabled(true);
	       	buttonPlay.setEnabled(false);	       		
	   	}
	};

	Button.OnClickListener buttonFavouriteClickListener = new Button.OnClickListener(){
	   	public void onClick(View view)  {
	   		if (is_favourite) {
	   			dbHelper.deleteFromFavourites(listen_url);
	   			buttonFavourite.setText(R.string.favourite_add);
	   		}
	   		else {
	   			dbHelper.insertIntoFavourites(listen_url);
	   			buttonFavourite.setText(R.string.favourite_del);
	   		}
	   	}
	};
	
	Button.OnClickListener buttonHomeClickListener = new Button.OnClickListener(){
	   	public void onClick(View view)  {
	        Intent intent = new Intent(_StationListenActivity.this, MainActivityCircle.class);
	        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(intent);
	   	}
	};
} 
