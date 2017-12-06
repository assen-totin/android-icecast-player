package com.voody.icecast.player;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import static android.R.attr.id;

public class StationListActivity extends ListActivity {
	SQLiteHelper dbHelper;
	ImageView buttonHome;
	Button buttonFilter, buttonReset;
	String mode, genre, query;
	String[][] stations, filtered;
	Boolean filterOn = false;

	public void onCreate(Bundle savedInstanceState) {
		Bundle recvBundle;

		super.onCreate(savedInstanceState);

		setContentView(R.layout.station_list);

		buttonFilter = (Button)findViewById(R.id.filter_button);
		buttonFilter.setOnClickListener(buttonFilterClickListener);

		buttonReset = (Button)findViewById(R.id.reset_button);
		buttonReset.setOnClickListener(buttonResetClickListener);

		buttonHome = (ImageView)findViewById(R.id.go_home);
		buttonHome.setOnTouchListener(buttonHomeTouchListener);
		
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
		else if(mode.compareTo("recent") == 0) {
			setTitle(getResources().getText(R.string.title_activity_recent));
			stations = dbHelper.getStationsRecent();
		}
		else if(mode.compareTo("favourites") == 0) {
			setTitle(getResources().getText(R.string.title_activity_favourites));
			stations = dbHelper.getStationsFavourites();
		}

		updateList(stations);
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
		String[][] st = (filterOn) ? filtered : stations;

		sendBundle.putString("server_name", st[position][0]);
		sendBundle.putString("listen_url", st[position][1]);
		sendBundle.putString("bitrate", st[position][2]);
		sendBundle.putString("rowid", st[position][3]);
		
		Intent intent = new Intent(view.getContext(), StationListenActivityImg.class);
		intent.putExtras(sendBundle);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	private void updateList(String[][] s) {
		ArrayList<String> display = new ArrayList<String>();

		if ((s == null) || (s.length == 0)) {
			Toast toast = Toast.makeText(StationListActivity.this, getString(R.string.no_results), Toast.LENGTH_SHORT);
			toast.show();
		}
		else {
			for (int i=0; i< s.length; i++)
				display.add(s[i][0] + " (" + s[i][2] + " kbps)");
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, display );
		setListAdapter(adapter);
	}

	Button.OnTouchListener buttonHomeTouchListener = new Button.OnTouchListener(){
		public boolean onTouch(View view, MotionEvent event)  {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				Intent intent = new Intent(StationListActivity.this, MainActivityCircle.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			return true;
		}
	};

	Button.OnClickListener buttonFilterClickListener = new Button.OnClickListener(){
		public void onClick(View view)  {
			int j = 0;
			ArrayList<Integer> al = new ArrayList<Integer>();
			EditText editText = (EditText) findViewById(R.id.filter_form);
			String query = editText.getText().toString();

			if (query == "")
				return;

			filterOn = true;

			// Loop around original list of station and filter further
			for (int i=0; i<stations.length; i++) {
				if (stations[i][0].toLowerCase().indexOf(query.trim().toLowerCase()) > -1)
					al.add(i);
			}

			filtered = new String[al.size()][4];

			for (int i=0; i<al.size(); i++) {
				filtered[j][0] = stations[al.get(i)][0];
				filtered[j][1] = stations[al.get(i)][1];
				filtered[j][2] = stations[al.get(i)][2];
				filtered[j][3] = stations[al.get(i)][3];
				j++;
			}

			updateList(filtered);
		}
	};

	Button.OnClickListener buttonResetClickListener = new Button.OnClickListener(){
		public void onClick(View view)  {
			EditText edit_text = (EditText) findViewById(R.id.filter_form);
			edit_text.setText(getResources().getText(R.string.empty));
			filterOn = false;
			updateList(stations);
		}
	};
} 