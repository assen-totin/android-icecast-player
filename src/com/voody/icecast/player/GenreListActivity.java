package com.voody.icecast.player;

import java.util.ArrayList;

import android.app.Activity;
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

public class GenreListActivity extends ListActivity {
	SQLiteHelper dbHelper;
	ImageView buttonHome;
	Button buttonSearch;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.genre_list);

		buttonSearch = (Button)findViewById(R.id.search_button);
		buttonSearch.setOnClickListener(buttonSearchClickListener);

		buttonHome = (ImageView)findViewById(R.id.go_home);
		buttonHome.setOnTouchListener(buttonHomeTouchListener);

		dbHelper = new SQLiteHelper(GenreListActivity.this);

		updateList(null);
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

	private void updateList(String query) {
		ArrayList<String> genres = dbHelper.getGenres(query);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, genres);
		setListAdapter(adapter);
	}

	Button.OnTouchListener buttonHomeTouchListener = new Button.OnTouchListener(){
		public boolean onTouch(View view, MotionEvent event)  {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				Intent intent = new Intent(GenreListActivity.this, MainActivityCircle.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			return true;
		}
	};

	Button.OnClickListener buttonSearchClickListener = new Button.OnClickListener(){
		public void onClick(View view)  {
			EditText edit_text = (EditText) findViewById(R.id.search_form);
			String query = edit_text.getText().toString();
			updateList(query);
		}
	};
}
