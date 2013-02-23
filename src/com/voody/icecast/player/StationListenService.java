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

public class StationListenService extends Service {
	private MediaPlayer mediaPlayer;
	String listen_url;
	Bundle recvBundle;
	
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
	}
	
	public int onStartCommand(Intent intent, int flags, int startid) {
		recvBundle = intent.getExtras();
		if (recvBundle != null)
			listen_url = recvBundle.getString("listen_url");
              
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
    
    /*
    // Here is how to send reply back to the registered clients. 
    private void sendMessageToUI(int intvaluetosend) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));

                //Send data as a String
                Bundle b = new Bundle();
                b.putString("str1", "ab" + intvaluetosend + "cd");
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
     */
} 
