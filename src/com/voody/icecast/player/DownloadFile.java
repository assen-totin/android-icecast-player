package com.voody.icecast.player;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import android.content.Context;
import android.content.Intent;

public class DownloadFile extends Activity { 
    //initialize our progress dialog/bar
    private ProgressDialog mProgressDialog;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
     
    //defining file name and url
    public String fileName = "yp.xml";
    public String fileURL = "http://dir.xiph.org/yp.xml";
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setting some display
        setContentView(R.layout.download_file);
         
        //executing the asynctask
        new DownloadFileAsync().execute(fileURL);
    }
   
    //this is our download file asynctask
    class DownloadFileAsync extends AsyncTask<String, String, String> {    	   	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
       
        @Override
        protected String doInBackground(String... aurl) {
        	try {
            	// Call a HEAD method to obtain raw size -
            	// needed for progress bar when the download is zipped
            	URL url_size = new URL(fileURL);
            	HttpURLConnection con_size = (HttpURLConnection) url_size.openConnection();
            	con_size.setRequestMethod("HEAD");
            	// Disable compression as we want raw size
            	con_size.setRequestProperty("Accept-Encoding", "identity");
            	//con_size.setDoOutput(true);
            	con_size.connect();
            	int lengthOfFile = con_size.getContentLength();
            	
            	System.err.print("LENGTH_OF_FILE: " + lengthOfFile);
            	
                // Actual download
                URL url = new URL(fileURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoOutput(true);
                con.connect();
               
                //lenghtOfFile is used for calculating download progress
                int lenghtOfFileDnld = con.getContentLength();
                if (lenghtOfFileDnld == -1) {
                	// We have zipped download, which does not report Content-Length
                	lenghtOfFileDnld = (int) lengthOfFile / 10;
                }
                
                FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

                //file input is from the url
                InputStream is = con.getInputStream();

                //here's the download code
                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
               
                while ((len1 = is.read(buffer)) > 0) {
                    total += len1; 
                    int progress_val = (int)((total*100)/lenghtOfFileDnld);
                    if (progress_val > 99) {
                    	progress_val = 100;
                    }
                    publishProgress("" + progress_val);
                    fos.write(buffer, 0, len1);
                }
                fos.close();
               
            } catch (Exception e) {
                Log.e("Icecast Player", e.getMessage());
            }
           
            return null;
        }
       
        protected void onProgressUpdate(String... progress) {
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            //dismiss the dialog after the file was downloaded
        	dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
	     
	        Intent intent = new Intent(DownloadFile.this, ProcessFile.class);
	        startActivity(intent);
        	
            finish();
        }
    }
     
    //our progress bar settings
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS: //we set this to 0
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }
}