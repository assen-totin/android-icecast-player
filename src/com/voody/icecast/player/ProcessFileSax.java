package com.voody.icecast.player;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class ProcessFileSax extends Activity { 
    // Init progress dialog/bar
    private ProgressDialog mProgressDialog;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
     
    // Define file name and genre separator
    public String fileName = "yp.xml";
    public String separator = " ";
    public String downloadName = "";
    public int mode = 1;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extra = this.getIntent().getExtras();
        if (extra != null) {
            fileName = extra.getString("filename");
            separator = extra.getString("separator");
            downloadName = extra.getString("name");
            mode = extra.getInt("mode");
        }

        // Set display
        setContentView(R.layout.download_file);
               
        // Execute the asynctask
        new ProcessFileAsync().execute(fileName);
    }
   
    //this is our download file asynctask
    class ProcessFileAsync extends AsyncTask<String, String, String> {    	   	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
       
        @Override
        protected String doInBackground(String... aurl) {
            // Init DB
            SQLiteHelper dbHelper = new SQLiteHelper(ProcessFileSax.this);
            if (mode == 1) {
                dbHelper.deleteFromStations();
                dbHelper.insertIntoUpdates();
            }

            publishProgress("" + 1);

            SaxDataHandler dataHandler = null;
            try {
            	SAXParserFactory spf = SAXParserFactory.newInstance();
            	SAXParser sp = spf.newSAXParser();
            	XMLReader xr = sp.getXMLReader();

                switch(mode) {
                    case 1:
                        dataHandler = new SaxDataHandler1();
                        break;
                    case 2:
                        dataHandler = new SaxDataHandler2();
                        break;
                }

                xr.setContentHandler(dataHandler);
            	FileInputStream fis = openFileInput(fileName);
            	xr.parse(new InputSource(fis));
            }
            catch(ParserConfigurationException pce) { 
            	Log.e("SAX XML", "sax parse error", pce); 
            } catch(SAXException se) { 
            	Log.e("SAX XML", "sax error", se); 
            } catch(IOException ioe) { 
            	Log.e("SAX XML", "sax parse io error", ioe); 
            } 
            
			publishProgress("" + 10);
			
            // Process the SAX into SQL table
            for (int i = 0; i < dataHandler.getData().getServerName().size(); i++) {
            	String server_name = dataHandler.getData().getServerName().get(i);
            	String listen_url = dataHandler.getData().getListenUrl().get(i);
            	String bitrate = dataHandler.getData().getBitrate().get(i);
            	String genre = dataHandler.getData().getGenre().get(i);

            	listen_url = listen_url.replace("\'","&apos");
            	server_name = server_name.replace("\'","&apos");

            	String[] genre_single = genre.split(separator);
            	for (int j=0; j<genre_single.length; j++) {
					genre_single[j] = genre_single[j].replace("\'","");
            		dbHelper.insertIntoStations(server_name, listen_url, bitrate, genre_single[j]);
            	}

            	if (i % 100 == 0)
            		publishProgress("" + (10 + 90*i/dataHandler.getData().getServerName().size()));
            }

            publishProgress("" + 100);
            
            dbHelper.close();
            
            return null;
        }
       
        protected void onProgressUpdate(String... progress) {
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            //dismiss the dialog after the file was downloaded
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
                    
            finish();
        }
    }
     
    //our progress bar settings
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS: //we set this to 0
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getResources().getText(R.string.processing) + " " + downloadName + "...");
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
