package com.voody.icecast.player;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

//import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
//import android.util.Log;


public class StationListenService extends Service {
	private MediaPlayer mediaPlayer;
	String listen_url;
	Bundle recvBundle;
	WifiLock wifiLock;
	
	private int playback_status = 0;	// 0 - initializing, 3 prepared, -1 error, 5 paused, 10 playing 
	
	long startTime;
	Handler timer = new Handler();
	
	static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    final Messenger mMessenger = new Messenger(new IncomingHandler(this)); // Target we publish for clients to send messages to IncomingHandler.
    
	@Override
    public void onCreate() {
		 mediaPlayer = new MediaPlayer();
		 mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		 mediaPlayer.setOnErrorListener(errListener);
		 mediaPlayer.setOnPreparedListener(prepListener);
		 
		 mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		 
		 wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
		 wifiLock.acquire();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		recvBundle = intent.getExtras();
		if (recvBundle != null)
			listen_url = recvBundle.getString("listen_url");
		
		//listen_url = "http://192.168.168.104:8000/stream.ogg";
		
		// Try to add a slash at the end of the URL if it is directory (i.e. no known extension)
		/*
		String extension = MimeTypeMap.getFileExtensionFromUrl(listen_url);
		Log.e("DEBUG", "SERVICE extension retrieved " + extension);
	    if (extension != null) {
	    	Log.e("DEBUG", "SERVICE extension is null");
	        MimeTypeMap mime = MimeTypeMap.getSingleton();
	        String mime_type = mime.getMimeTypeFromExtension(extension);
	        Log.e("DEBUG", "SERVICE MIME type retrieved " + mime_type);
	        if (mime_type == null) {
	        	Log.e("DEBUG", "SERVICE mime_type is null");
	        	if (listen_url.substring(listen_url.length() - 1) != "/") {
	        		Log.e("DEBUG", "SERVICE listen_url does not end with slash");
	        		//listen_url += "/";
	        		//listen_url += ".m3u";
	        		Log.e("DEBUG", "SERVICE added slash to URL, new is " + listen_url);
	        	}
	        }
	    }
		*/
		
		mediaPlayer.reset();
		
		try {
			mediaPlayer.setDataSource(this, Uri.parse(listen_url));
		} catch (IllegalArgumentException e) {
			//Log.e("DEBUG", "setDataSource IllegalArgumentException");
			playback_status = -1;
		} catch (IllegalStateException e) {
			//Log.e("DEBUG", "setDataSource IllegalStateException");
			playback_status = -1;
		} catch (IOException e) {
			//Log.e("DEBUG", "setDataSource IOException");
			playback_status = -1;
		}
		
		try {
			mediaPlayer.prepareAsync();
		} catch (IllegalStateException e) {
			//Log.e("DEBUG", "prepareAsync IllegalStateException");
			playback_status = -1;
		} 
			
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		//Log.e("DEBUG", "SERVICE Called onDestroy");
		mediaPlayer.stop();
		mediaPlayer.release();
		wifiLock.release();
		super.onDestroy();
	}
	
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
	
    MediaPlayer.OnErrorListener errListener = new MediaPlayer.OnErrorListener() {
    	@Override
    	public boolean onError (MediaPlayer mp, int what, int extra) {
    			playback_status = -1;
    			sendMessageToUI(playback_status);
    		return true;
    	}
    };
    
    MediaPlayer.OnPreparedListener prepListener = new MediaPlayer.OnPreparedListener() {
    	@Override
		public void onPrepared(MediaPlayer mediaPlayer) {
			mediaPlayer.start();
			playback_status = 10;
			sendMessageToUI(playback_status);
		}
	};
	
    // This is a bit cumbersome, but having the handler 'static' avoids potential
    // memory leak when a message is put in a queue when the client gone. 
    static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
    	private final WeakReference<StationListenService> mService;
    	
    	IncomingHandler(StationListenService service) {
    		mService = new WeakReference<StationListenService>(service);
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	StationListenService service = mService.get();       	
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
            	service.mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                service.mClients.remove(msg.replyTo);
                break;
            case MSG_SET_INT_VALUE:
            	if (msg.arg1 == 10) {
            		// Start playback
                	if (service.playback_status >= 5) {
                		service.mediaPlayer.start();
                		service.playback_status = 10;
                	}
            	}
                else if (msg.arg1 == 5) {
                	// Pause playback
                	if (service.playback_status >= 5) {
                		service.mediaPlayer.pause();
                		service.playback_status = 5;
                	}
                }
            	service.sendMessageToUI(service.playback_status);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    // Here is how to send reply back to the registered clients. 
    private void sendMessageToUI(int intvaluetosend) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));
                /*
                //Send data as a String
                Bundle b = new Bundle();
                b.putString("reply", "abc" + intvaluetosend);
                Message msg = Message.obtain(null, MSG_SET_INT_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);
                */
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
    
} 
