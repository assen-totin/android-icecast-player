package com.voody.icecast.player;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

//import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class StationListenService extends Service {
	private MediaPlayer mediaPlayer;
	String listen_url;
	Bundle recvBundle;
	private int now_playing = 0;
	
	long startTime;
	Handler timer = new Handler();
	
	private static boolean isAlive = true;
	static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;
    //private NotificationManager nm;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    final Messenger mMessenger = new Messenger(new IncomingHandler(this)); // Target we publish for clients to send messages to IncomingHandler.
    
	public void onCreate() {
		 mediaPlayer = new MediaPlayer();
		 mediaPlayer.setOnErrorListener(errListener);
	}
	
	public int onStartCommand(Intent intent, int flags, int startid) {
		recvBundle = intent.getExtras();
		if (recvBundle != null)
			listen_url = recvBundle.getString("listen_url");
              
		mediaPlayer.reset();
		
		try {
			mediaPlayer.setDataSource(listen_url);
		} catch (IllegalArgumentException e) {
			now_playing = -1;
		} catch (IllegalStateException e) {
			now_playing = -1;
		} catch (IOException e) {
			now_playing = -1;
		}
		
		try {
			mediaPlayer.prepare();
		} catch (IOException e) {
			now_playing = -1;
		} catch (IllegalStateException e) {
			now_playing = -1;
		} 
		
		
		// If no error so far, start
		if (now_playing == 0) {
			mediaPlayer.start();
			now_playing = 1;
			sendMessageToUI(now_playing);
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		mediaPlayer.stop();
		mediaPlayer.release();
		isAlive = false;
		super.onDestroy();
	}	
	
    public static boolean isAlive(){
        return isAlive;
    }
	
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
	
    MediaPlayer.OnErrorListener errListener = new MediaPlayer.OnErrorListener() {
    	public boolean onError (MediaPlayer mp, int what, int extra) {
    			Log.e("DEBUG", "SERVICE onErrorListener");
    			now_playing = -1;
    			sendMessageToUI(now_playing);
    		return true;
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
            	Log.e("DEBUG", "SERVICE MSG_REGISTER_CLIENT");
                break;
            case MSG_UNREGISTER_CLIENT:
                service.mClients.remove(msg.replyTo);
                break;
            case MSG_SET_INT_VALUE:
            	if (msg.arg1 == 1)
                	service.mediaPlayer.start();
                else if (msg.arg1 == 2)
                	service.mediaPlayer.pause();
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    
    // Here is how to send reply back to the registered clients. 
    private void sendMessageToUI(int intvaluetosend) {
    	Log.e("DEBUG", "SERVICE in sendMessageToUI: "+intvaluetosend);
        for (int i=mClients.size()-1; i>=0; i--) {
        	Log.e("DEBUG", "SERVICE in sendMessageToUI client ID: "+i);
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));
                Log.e("DEBUG", "SERVICE SENT: "+intvaluetosend);
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
