package com.voody.icecast.player;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class StationListenActivityImg extends Activity {
	private MediaPlayer mediaPlayer = new MediaPlayer();
	ImageView buttonStop, buttonPlay, buttonFavourite, buttonHome;
	TextView textViewServerName, textViewListenUrl, textViewBitrate, textViewTimer;
	String server_name, listen_url, bitrate;
	Boolean is_favourite = false;
	Bundle recvBundle;
	
	SQLiteHelper dbHelper = new SQLiteHelper(StationListenActivityImg.this);
	
	long startTime;
	Handler timer = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen_url_img);
		
		recvBundle = this.getIntent().getExtras();
		if (recvBundle == null)
			recvBundle = savedInstanceState;
		
    	server_name = recvBundle.getString("server_name");
    	listen_url = recvBundle.getString("listen_url");
    	bitrate = recvBundle.getString("bitrate");
		 	
		dbHelper.insertIntoRecent(listen_url);

        buttonStop = (ImageView)findViewById(R.id.stop);
        buttonStop.setOnTouchListener(buttonStopTouchListener);
        buttonStop.setEnabled(false);
		
        buttonPlay = (ImageView)findViewById(R.id.play);
        buttonPlay.setOnTouchListener(buttonPlayTouchListener);
        buttonPlay.setEnabled(false);
        
        buttonFavourite = (ImageView)findViewById(R.id.favourite);
        buttonFavourite.setOnTouchListener(buttonFavouriteTouchListener);
		
        buttonHome = (ImageView)findViewById(R.id.go_home);
        buttonHome.setOnTouchListener(buttonHomeTouchListener);
        
        textViewServerName = (TextView) findViewById(R.id.textView_server_name_2);
        textViewServerName.setText(server_name);
        
        textViewListenUrl = (TextView) findViewById(R.id.textView_listen_url_2);
        textViewListenUrl.setText(listen_url);
        
        textViewBitrate = (TextView) findViewById(R.id.textView_bitrate_2);
        textViewBitrate.setText(bitrate + " kbps");
        
        textViewTimer = (TextView) findViewById(R.id.textView_timer_2);
        
        if (dbHelper.isFavourite(listen_url)) {
        	is_favourite = true;
        	buttonFavourite.setImageResource(R.drawable.b3_del);
        	buttonFavourite.setContentDescription(getString(R.string.cd_favourite_del));
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
		
		startTime = System.currentTimeMillis();
		timer.postDelayed(runTimer, 100);
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
	
	Button.OnTouchListener buttonStopTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			mediaPlayer.pause();
	   			buttonStop.setEnabled(false);
	   			buttonStop.setImageResource(R.drawable.b2_off);
	   			buttonPlay.setEnabled(true);
	   			buttonPlay.setImageResource(R.drawable.b1_on);
	   		}
	       	return true;
	   	}
	};
		
	Button.OnTouchListener buttonPlayTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			mediaPlayer.start();
	   			buttonStop.setEnabled(true);
	   			buttonStop.setImageResource(R.drawable.b2_on);
	   			buttonPlay.setEnabled(false);
	   			buttonPlay.setImageResource(R.drawable.b1_off);
	   		}
	       	return true;
	   	}
	};

	Button.OnTouchListener buttonFavouriteTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			if (is_favourite) {
	   				dbHelper.deleteFromFavourites(listen_url);
	   				buttonFavourite.setImageResource(R.drawable.b3_add);
	   				is_favourite = false;
	   			}
	   			else {
	   				dbHelper.insertIntoFavourites(listen_url);
	   				buttonFavourite.setImageResource(R.drawable.b3_del);
	   				is_favourite = true;
	   			}
	   		}
	   		return true;
	   	}
	};
	
	Button.OnTouchListener buttonHomeTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			Intent intent = new Intent(StationListenActivityImg.this, MainActivityCircle.class);
	   			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	   			startActivity(intent);
	   		}
	        return true;
	   	}
	};
	
	Runnable runTimer = new Runnable(){
        public void run() {
           long diffTimeMillis = System.currentTimeMillis() - startTime;
           int diffTime = (int) (diffTimeMillis / 1000);
           int hours = diffTime / 3600;
           int remTime = diffTime % 3600;
           int minutes = remTime / 60;
           int seconds = remTime % 60;

           textViewTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
           
           timer.postDelayed(runTimer, 1000);
        }
    };
} 
