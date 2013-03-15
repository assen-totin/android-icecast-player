package com.voody.icecast.player;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
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
	TextView textViewServerName, textViewListenUrl, textViewBitrate, textViewTimer;
	ImageView buttonPause, buttonPlay, buttonFavourite, buttonHome;
	Boolean buttonPlayState = false;
	Boolean buttonPauseState = false;
	
	Bundle recvBundle, sendBundle;
	Intent serviceIntent;
	ComponentName service_component_name;
	private SharedPreferences mPrefs;

	String server_name, listen_url, bitrate;
	Boolean is_favourite = false;
	
	SQLiteHelper dbHelper;
	
	SharedPreferences.Editor ed;
	
	Class<?> serviceClass = null;
	
	long startTime = 0;
	Handler runTimerHandler = new Handler();
	Handler runDelayedHandler = new Handler();
	
	int service_status = 0;
	Boolean keep_playing = false;
	Boolean first_run = true;
    
	final Messenger mMessenger = new Messenger(new IncomingHandler(this));
	Messenger mService = null;
    boolean mIsBound = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.e("DEBUG", "Called onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen_url_img);

		buttonPause = (ImageView)findViewById(R.id.stop);
        buttonPause.setOnTouchListener(buttonPauseTouchListener);

        buttonPlay = (ImageView)findViewById(R.id.play);
        buttonPlay.setOnTouchListener(buttonPlayTouchListener);
        
        buttonFavourite = (ImageView)findViewById(R.id.favourite);
        buttonFavourite.setOnTouchListener(buttonFavouriteTouchListener);
		
        buttonHome = (ImageView)findViewById(R.id.go_home);
        buttonHome.setOnTouchListener(buttonHomeTouchListener);

        textViewServerName = (TextView) findViewById(R.id.textView_server_name_2);
        textViewListenUrl = (TextView) findViewById(R.id.textView_listen_url_2);
        textViewBitrate = (TextView) findViewById(R.id.textView_bitrate_2);
        textViewTimer = (TextView) findViewById(R.id.textView_timer_2);
        
		//Prefer savedInstanceState to original intent bundle
		if (savedInstanceState == null) {
			Log.e("DEBUG", "onCreate savedInstanceState == null");
			recvBundle = this.getIntent().getExtras();
		}
		else {
			Log.e("DEBUG", "onCreate savedInstanceState != null");
			recvBundle = savedInstanceState;
			service_component_name = recvBundle.getParcelable("service_component_name");
			buttonPauseState = recvBundle.getBoolean("buttonPauseState");	
	        buttonPlayState = recvBundle.getBoolean("buttonPlayState");
		}
			
    	server_name = recvBundle.getString("server_name");
    	textViewServerName.setText(server_name);
    	
    	listen_url = recvBundle.getString("listen_url");
    	textViewListenUrl.setText(listen_url);
    	
    	bitrate = recvBundle.getString("bitrate");         
        textViewBitrate.setText(bitrate + " kbps");
        
        keep_playing = recvBundle.getBoolean("keep_playing");
        if (keep_playing) {
        	Log.e("DEBUG", "onCreate keep_playing");
        	try {
				serviceClass = Class.forName(service_component_name.getClassName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        	
        	setButtonsState();
        	
        	first_run = false;
        }
	}
	
	@Override
	public void onRestart() {
		Log.e("DEBUG", "Called onRestart");
		super.onRestart();
	}
	
	@Override
	public void onStart() {
		Log.e("DEBUG", "Called onStart");
		super.onStart();
				
        if (first_run) {
        	// We have been launched, not resurrected for config (screen orientation) change
        	Log.e("DEBUG", "onStart first_run && !keep_playing");
        	 
        	//Start our service
        	sendBundle = new Bundle();
        	sendBundle.putString("listen_url", listen_url);
        	serviceIntent = new Intent(this, StationListenService.class);
        	serviceIntent.putExtras(sendBundle);

        	// Start the service (the playback will begin as soon as it is ready) and save its ComponenName 
        	service_component_name = startService(serviceIntent);     	
        	try {
				serviceClass = Class.forName(service_component_name.getClassName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        	       			
			// Schedule a query to the service - 1 second from now (messaging is slow to wake up)
			runDelayedHandler.postDelayed(runDelayed, 1000);
        } 
        
        //We only need this here to wake-up the messenger service (connection is asynchronous and non-blocking)
        doBindService();   
	}
	
	@Override
	public void onResume() {
		Log.e("DEBUG", "Called onResume");
		super.onResume();
		
		dbHelper = new SQLiteHelper(StationListenActivityImg.this);
        if (dbHelper.isFavourite(listen_url)) {
        	is_favourite = true;
        	buttonFavourite.setImageResource(R.drawable.b3_del);
        	buttonFavourite.setContentDescription(getString(R.string.cd_favourite_del));
        }
		
		mPrefs = getPreferences(0);
		if (!first_run) {
			Log.e("DEBUG", "onResume !first_run");
			buttonPlayState = mPrefs.getBoolean("buttonPlayState", false);
			buttonPauseState = mPrefs.getBoolean("buttonPauseState", false);
			startTime = mPrefs.getLong("startTime", System.currentTimeMillis());
			runTimerHandler.postDelayed(runTimer, 100);
		}
		else {
			first_run = false;
			dbHelper.insertIntoRecent(listen_url);
		}
		
		setButtonsState();
               
        // We need this here despite the default because of task-switching
        keep_playing = false;
	}
	
	@Override
	public void onPause() {
		Log.e("DEBUG", "Called onPause");
		super.onPause();
        ed = mPrefs.edit();
        ed.putLong("startTime", startTime);
        ed.putBoolean("buttonPlayState", buttonPlayState);
        ed.putBoolean("buttonPauseState", buttonPauseState);
        ed.commit();
	}
	
	@Override
	public void onStop() {
		Log.e("DEBUG", "Called onStop");
		doUnbindService();
		dbHelper.close();
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		Log.e("DEBUG", "Called onDestroy");
		if (!keep_playing) {
			Log.e("DEBUG", "onDestroy !keep_playing");
						
			stopService(new Intent(this, serviceClass));
		}
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		Log.e("DEBUG", "Called savedInstanceState");
			
		savedInstanceState.putString("server_name", server_name);
		savedInstanceState.putString("listen_url", listen_url);
		savedInstanceState.putString("bitrate", bitrate);
		savedInstanceState.putBoolean("buttonPlayState", buttonPlayState);
		savedInstanceState.putBoolean("buttonPauseState", buttonPauseState);
		savedInstanceState.putBoolean("keep_playing", keep_playing);
		savedInstanceState.putParcelable("service_component_name", service_component_name);
		
		keep_playing = true;
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.e("DEBUG", "Called restoreInstanceState");
	}
	
	Button.OnTouchListener buttonPauseTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			// 5 means 'pause'
	   			sendMessageToService(5);
	   			buttonPauseState = false;
	   			buttonPlayState = true;
	   			setButtonsState();
	   		}
	       	return true;
	   	}
	};
		
	Button.OnTouchListener buttonPlayTouchListener = new Button.OnTouchListener(){
	   	public boolean onTouch(View view, MotionEvent event)  {
	   		if(event.getAction() == MotionEvent.ACTION_DOWN) {
	   			// 10 means 'play'
	   			sendMessageToService(10);
	   			buttonPauseState = true;
	   			buttonPlayState = false;
	   			setButtonsState();
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
	
	Runnable runDelayed = new Runnable(){
        public void run() {
        	Log.e("DEBUG", "Called runDelayed");
    		// Query the service if it is running, but only if have not received error message
        	if (service_status >=0) {
        		Log.e("DEBUG", "runDelayed service_status >= 0");
        		sendMessageToService(99);
        	}
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
           
           runTimerHandler.postDelayed(runTimer, 1000);
        }
    };
    
    void setButtonsState() {
    	if (buttonPlayState) {
    		buttonPlay.setEnabled(true);
    		buttonPlay.setImageResource(R.drawable.b1_on);
    	}
    	else {
    		buttonPlay.setEnabled(false);
    		buttonPlay.setImageResource(R.drawable.b1_off);
    	}
    	
    	if (buttonPauseState) {
    		buttonPause.setEnabled(true);
    		buttonPause.setImageResource(R.drawable.b2_on);
    	}
    	else {
    		buttonPause.setEnabled(false);
    		buttonPause.setImageResource(R.drawable.b2_off);
    	}
    }
    
    void startPlaying() {
    	Log.e("DEBUG", "Called startPlaying");
    	
    	if (service_status == 10)
    		return;
    	
    	service_status = 10;
    	if (startTime == 0) {
    		// we have just been launched
    		Log.e("DEBUG", "startPlaying startTime == 0");
    		startTime = System.currentTimeMillis();
    	}
    	
    	runTimerHandler.postDelayed(runTimer, 100);
		buttonPauseState = true;
		buttonPlayState = false;
		setButtonsState();
    }

    void pausePlaying() {
    	service_status = 5;
 		buttonPauseState = false;
 		buttonPlayState = true;
 		setButtonsState();
     }
    
    void showError() {
    	if (service_status == -1)
    		return;
    	
    	service_status = -1;
    	Toast toast = Toast.makeText(this, getString(R.string.unable_to_load_station), Toast.LENGTH_SHORT);
    	toast.show();
    }

    void showLoading() {
    	Log.e("DEBUG", "Called showLoading");
    	service_status = 0;
    	runDelayedHandler.postDelayed(runDelayed, 5000);
        Toast toast1 = Toast.makeText(this, getString(R.string.loading_station), Toast.LENGTH_SHORT);
        toast1.show();
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
    		// This is non-blocking and returns immediately, even if mConnection is not ready yet and mService is still NULL
    		bindService(new Intent(this, serviceClass), mConnection, Context.BIND_AUTO_CREATE);
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
            // If we have received the service, and hence registered with it, then unregister
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
   
    static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
    	private final WeakReference<StationListenActivityImg> mService;
    	
    	IncomingHandler(StationListenActivityImg service) {
    		mService = new WeakReference<StationListenActivityImg>(service);
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	StationListenActivityImg service = mService.get();   
            switch (msg.what) {
            case StationListenService.MSG_SET_INT_VALUE:
                if (msg.arg1 == -1)
                	service.showError();
                else if (msg.arg1 == 0)
                	service.showLoading();
                else if (msg.arg1 == 5)
                	service.pausePlaying();               	
                else if (msg.arg1 == 10)
                	service.startPlaying();
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
} 
