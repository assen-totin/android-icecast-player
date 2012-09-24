package com.voody.icecast.player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
    //initialize our progress dialog/bar
    private ProgressDialog mProgressDialog;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
     
    //defining file name and url
    public String fileName = "yp.xml";
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setting some display
        setContentView(R.layout.download_file);
               
        //executing the asynctask
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
            dbHelper.deleteFromStations();
            dbHelper.insertIntoUpdates();
            
            //dbHelper.deleteAllFavourites();
            //dbHelper.deleteAllRecent();            

            try {
            	SAXParserFactory spf = SAXParserFactory.newInstance();
            	SAXParser sp = spf.newSAXParser();
            	XMLReader xr = sp.getXMLReader();
            	SaxDataHandler dataHandler = new SaxDataHandler();
            	xr.setContentHandler(dataHandler);
            	xr.parse(new InputSource(new FileInputStream(fileName)));
            }
            catch(ParserConfigurationException pce) { 
            	Log.e("SAX XML", "sax parse error", pce); 
            } catch(SAXException se) { 
            	Log.e("SAX XML", "sax error", se); 
            } catch(IOException ioe) { 
            	Log.e("SAX XML", "sax parse io error", ioe); 
            } 

            
            // Parse the XML from file to DOM object
            XMLParser parser = new XMLParser();

			Document dom = null;
			try {
				dom = parser.getDomElement(ProcessFileSax.this, fileName);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
            
			publishProgress("" + 10);
			
            // Process the DOM into SQL table
            NodeList nl = dom.getElementsByTagName("entry");
            
            for (int i = 0; i < nl.getLength(); i++) {
            	Element e = (Element) nl.item(i);
            	String listen_url = parser.getValue(e, "listen_url");
            	String server_name = parser.getValue(e, "server_name");
            	String bitrate = parser.getValue(e, "bitrate");
            	String genre = parser.getValue(e, "genre");

            	listen_url.replace("'","&apos");
            	server_name.replace("'","&apos");
            	
            	String[] genre_single = genre.split(" ");
            	for (int j=0; j<genre_single.length; j++) {
            		genre_single[j].replace("'","&apos");
            		dbHelper.insertIntoStations(server_name, listen_url, bitrate, genre_single[j]);
            	}
            	
            	if (i%100 == 0) {
            		publishProgress("" + (10 + 90*i/nl.getLength()));
            	}
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
                mProgressDialog.setMessage("Processing file...");
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

