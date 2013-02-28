package com.voody.icecast.player;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class StationListActivity extends ListActivity {
	String mode, genre, query;
	String[][] stations;
	SQLiteHelper dbHelper;
	Bundle recvBundle;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayList<String> display_name = new ArrayList<String>();
		
		recvBundle = this.getIntent().getExtras();
		if (recvBundle == null)
			recvBundle = savedInstanceState;
			
    	mode = recvBundle.getString("mode");
		
		dbHelper = new SQLiteHelper(StationListActivity.this);
		
		if(mode.compareTo("genre") == 0) {
			genre = recvBundle.getString("genre");
			stations = dbHelper.getStationsByGenre(genre);
		}
		else if (mode.compareTo("search") == 0) {
			query = recvBundle.getString("query");
			stations = dbHelper.getStationsSearch(query);
		}
		else if(mode.compareTo("recent") == 0)
			stations = dbHelper.getStationsRecent();
		else if(mode.compareTo("favourites") == 0) 
			stations = dbHelper.getStationsFavourites();
		
		checkStations(stations);
		
		for (int i=0; i< stations.length; i++) {
			display_name.add(stations[i][0] + " (" + stations[i][2] + " kbps)");
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, display_name );
		setListAdapter(adapter);
	}

	public void onDestroy() {
		super.onDestroy();
		dbHelper.close();
	}

	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("mode", mode);
		savedInstanceState.putString("query", query);
		savedInstanceState.putString("genre", genre);
	}
	
	@Override
	protected void onListItemClick(ListView l, View view, int position, long id) {
		Bundle sendBundle = new Bundle();
		sendBundle.putString("server_name", stations[position][0]);
		sendBundle.putString("listen_url", stations[position][1]);
		sendBundle.putString("bitrate", stations[position][2]);
		sendBundle.putLong("startTime", 0);
		sendBundle.putBoolean("buttonPlayState", false);
		sendBundle.putBoolean("buttonPauseState", false);
		
		Intent intent = new Intent(view.getContext(), StationListenActivityImg.class);
		intent.putExtras(sendBundle);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	
	private void checkStations(String[][] stations){
		if (stations.length == 0) {
			Toast toast = Toast.makeText(StationListActivity.this, getString(R.string.no_results), Toast.LENGTH_SHORT);
			toast.show();
			finish();
		}
	}
} 