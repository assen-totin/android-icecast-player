package com.voody.icecast.player;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

public class StationListenActivityImg extends Activity {
	ImageView buttonStop, buttonPlay, buttonFavourite, buttonHome;
	TextView textViewServerName, textViewListenUrl, textViewBitrate, textViewTimer;
	String server_name, listen_url, bitrate;
	Boolean is_favourite = false;
	Bundle recvBundle, sendBundle;
	Intent serviceIntent;
	
	SQLiteHelper dbHelper = new SQLiteHelper(StationListenActivityImg.this);
	
	long startTime = 0;
	Handler timer = new Handler();
	
	Boolean keep_playing = false;
    
	final Messenger mMessenger = new Messenger(new IncomingHandler(this));
	Messenger mService = null;
    boolean mIsBound = false;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen_url_img);
		
		recvBundle = this.getIntent().getExtras();
		if (recvBundle == null)
			recvBundle = savedInstanceState;
		
    	server_name = recvBundle.getString("server_name");
    	listen_url = recvBundle.getString("listen_url");
    	bitrate = recvBundle.getString("bitrate");
    	startTime = recvBundle.getLong("startTime");
		 	
		dbHelper.insertIntoRecent(listen_url);

        buttonStop = (ImageView)findViewById(R.id.stop);
        buttonStop.setOnTouchListener(buttonStopTouchListener);
        buttonStop.setEnabled(false);
        buttonStop.setImageResource(R.drawable.b2_off);
		
        buttonPlay = (ImageView)findViewById(R.id.play);
        buttonPlay.setOnTouchListener(buttonPlayTouchListener);
        buttonPlay.setEnabled(false);
        buttonPlay.setImageResource(R.drawable.b1_off);
        
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
        
        // We have been launched, not resurrected for config (screen orientation) change
        if (startTime == 0) {
        	// Start our service
        	sendBundle = new Bundle();
        	sendBundle.putString("listen_url", listen_url);
        	serviceIntent = new Intent(this, StationListenService.class);
        	serviceIntent.putExtras(sendBundle);
        	// Start the service - the playback won't yet begin
        	startService(serviceIntent);
        	// We only need this here to wake-up the messenger service (connection is asynchronous and non-blocking)
        	doBindService();
        	// Send a 'play' command after the service connection is up & ruinning
        	timer.postDelayed(startPlayback, 2000);
        	
			// we have just been launched
			startTime = System.currentTimeMillis();
        }
	}
	
	public void onStart() {
		super.onStart();
        // We need this here despite the default because of task-switching
        keep_playing = false;
	}
	
	public void onDestroy() {
		super.onDestroy();
		if (!keep_playing) {
			doUnbindService();
			stopService(serviceIntent);
		}
		dbHelper.close();
	}
	
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("server_name", server_name);
		savedInstanceState.putString("listen_url", listen_url);
		savedInstanceState.putString("bitrate", bitrate);
		savedInstanceState.putLong("startTime", startTime);
		// set the kepp_playing variable so that the onDestroy method does not stop the player service
		keep_playing = true;
	}
	
	Button.OnTouchListener buttonStopTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			// 2 means 'pause'
	   			sendMessageToService(2);
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
	   			// 1 means 'play'
	   			sendMessageToService(1);
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

	Runnable startPlayback = new Runnable(){
        public void run() {
        	sendMessageToService(1);
        }
    };
    
    void setButtons() {
		timer.postDelayed(runTimer, 100);
		buttonStop.setEnabled(true);
		buttonStop.setImageResource(R.drawable.b2_on);
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };
    
    private void sendMessageToService(int intvaluetosend) {
    	if (!mIsBound)
    		doBindService();
        
    	if (mIsBound) {
            try {
                Message msg = Message.obtain(null, StationListenService.MSG_SET_INT_VALUE, intvaluetosend, 0);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } 
            catch (RemoteException e) {
            }
        }
    }
    
    void doBindService() {
    	if (!mIsBound) {
    		// Note: this is non-blocking and returns immediately, even if mConnection is not ready yet and mService is still NULL
    		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    		if (mService != null) {
    			try {
    				Message msg = Message.obtain(null, StationListenService.MSG_REGISTER_CLIENT);
    				msg.replyTo = mMessenger;
    				mService.send(msg);
    			} 
    			catch (RemoteException e) {
    				// In this case the service has crashed before we could even do anything with it
    			}
    			mIsBound = true;
    		}
    	}
    }
    
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, StationListenService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } 
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    void showError() {  	
    	Toast toast = Toast.makeText(this, getString(R.string.unable_to_load_station), Toast.LENGTH_SHORT);
    	toast.show();
    }
    
    static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
    	private final WeakReference<StationListenActivityImg> mService;
    	
    	IncomingHandler(StationListenActivityImg service) {
    		mService = new WeakReference<StationListenActivityImg>(service);
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	StationListenActivityImg service = mService.get();   
        	//Log.e("DEBUG", "ACTIVITY RECV: " + msg.arg1);
            switch (msg.what) {
            /*
            case MSG_REGISTER_CLIENT:
                service.mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                service.mClients.remove(msg.replyTo);
                break;
            */
            case StationListenService.MSG_SET_INT_VALUE:
            	// 1 means failure to load station
                if (msg.arg1 == 1) 
                	service.setButtons();
                else
                	service.showError();
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
} 
