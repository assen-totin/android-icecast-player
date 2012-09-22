package com.voody.icecast.player;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GenreListActivity extends ListActivity {
	SQLiteHelper dbHelper;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		dbHelper = new SQLiteHelper(GenreListActivity.this);
		
		ArrayList<String> genres = dbHelper.getGenres();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, genres);
		setListAdapter(adapter);
	}
	
	public void onDestroy() {
		super.onDestroy();
		dbHelper.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View view, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Bundle sendBundle = new Bundle();
		sendBundle.putString("mode", "genre");
		sendBundle.putString("genre", item);
		Intent intent = new Intent(view.getContext(), StationListActivity.class);
		intent.putExtras(sendBundle);
		startActivity(intent);
	}
} 