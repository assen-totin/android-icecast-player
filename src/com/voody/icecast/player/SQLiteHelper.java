package com.voody.icecast.player;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "icecast.sqlite";
	private static final int DATABASE_VERSION = 2;

	private static final String DB_CREATE_TABLE_STATIONS = "CREATE TABLE stations (server_name VARCHAR(255), listen_url VARCHAR(255), bitrate VARCHAR(255), genre VARCHAR(255));";
	private static final String DB_CREATE_TABLE_FAVOURITES = "CREATE TABLE favourites (server_name VARCHAR(255), listen_url VARCHAR(255), bitrate VARCHAR(255), genre VARCHAR(255));";	
	private static final String DB_CREATE_TABLE_RECENT = "CREATE TABLE recent (server_name VARCHAR(255), listen_url VARCHAR(255), bitrate VARCHAR(255), genre VARCHAR(255), unix_timestamp VARCHAR(255));";
	private static final String DB_CREATE_TABLE_SETTINGS = "CREATE TABLE settings (name VARCHAR(255), val VARCHAR(255));";
	private static final String DB_CREATE_TABLE_UPDATES = "CREATE TABLE updates (unix_timestamp VARCHAR(255));";
	private static final String DB_CREATE_TABLE_VERSION = "CREATE TABLE version (version INT);";

	private static final String DB_INSERT_VERSION = "INSERT INTO version (version) VALUES (" + DATABASE_VERSION + ");";
	private static final String DB_INSERT_UPDATES = "INSERT INTO updates (unix_timestamp) VALUES ('1000');";
	private static final String DB_INSERT_SETTINGS1 = "INSERT INTO settings (name, val) VALUES ('auto_refresh','1')";
	private static final String DB_INSERT_SETTINGS2 = "INSERT INTO settings (name, val) VALUES ('refresh_days','7')";
	
	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DB_CREATE_TABLE_STATIONS);
		database.execSQL(DB_CREATE_TABLE_FAVOURITES);
		database.execSQL(DB_CREATE_TABLE_RECENT);
		database.execSQL(DB_CREATE_TABLE_SETTINGS);
		database.execSQL(DB_CREATE_TABLE_UPDATES);
		database.execSQL(DB_CREATE_TABLE_VERSION);
		
		database.execSQL(DB_INSERT_VERSION);
		database.execSQL(DB_INSERT_UPDATES);
		database.execSQL(DB_INSERT_SETTINGS1);
		database.execSQL(DB_INSERT_SETTINGS2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			database.execSQL("DELETE from settings where name='Favourites'");
			database.execSQL(DB_INSERT_SETTINGS1);
			database.execSQL(DB_INSERT_SETTINGS2);
		}
	}

	public void deleteFromStations() {
		String query = "DELETE FROM stations";
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL(query);
	}
	
	public int countStations() {
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.query("stations", null, null, null, null, null, null);
		return cursor.getCount();
	}
	
	public void insertIntoStations(String server_name, String listen_url, String bitrate, String genre_name_single) {
		String query = "INSERT INTO stations (server_name, listen_url, bitrate, genre) VALUES ('" + 
				server_name +
				"','" +
				listen_url +
				"','" +
				bitrate +
				"','" +
				genre_name_single +
				"');";
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL(query);
		//database.close();
	}

	public void insertIntoUpdates() {
		SQLiteDatabase database = this.getWritableDatabase();
		long unix_timestamp = System.currentTimeMillis()/1000;
		String query = "INSERT INTO updates (unix_timestamp) VALUES ('" +
				unix_timestamp +
				"')";
		database.execSQL(query);
	}	

	public long getUpdates() {
		long result=1000;
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT unix_timestamp FROM updates ORDER BY unix_timestamp DESC LIMIT 1", null);
		if (cursor.moveToFirst()) {
			result = cursor.getLong(0); // 0 is the first column
		}
		return result;
	}
	
	public ArrayList<String> getGenres(String query) {
		SQLiteDatabase database = this.getReadableDatabase();

		String q;
		if (query != null)
			q = "SELECT genre FROM stations WHERE (genre LIKE '%" + query + "%') GROUP BY genre ORDER BY genre";
		else
			q = "SELECT genre FROM stations GROUP BY genre ORDER BY genre";

		Cursor cursor = database.rawQuery(q, null);

		ArrayList<String> results = new ArrayList<String>();
		if (cursor.moveToFirst()) {
			do {
				results.add(cursor.getString(0)); // 0 is the first column
			} while (cursor.moveToNext());
		}
		return results;
	}

	public String[][] getStationsByGenre(String genre) {
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT server_name, listen_url, bitrate, ROWID FROM stations WHERE genre='" +
				genre +
				"' GROUP BY listen_url ORDER BY server_name", null);
		String[][] results = new String[cursor.getCount()][4];
		int i = 0;
		if (cursor.moveToFirst()) {
			do {
				results[i][0] = cursor.getString(0); // 0 is the first column
				results[i][1] = cursor.getString(1); 
				results[i][2] = cursor.getString(2);
				results[i][3] = cursor.getString(3);
				i++;
			} while (cursor.moveToNext());
		}
		return results;
	}

	public String[][] getStationsRecent() {
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT server_name, listen_url, bitrate, ROWID FROM recent GROUP BY listen_url ORDER BY unix_timestamp DESC LIMIT 20", null);
		String[][] results = new String[cursor.getCount()][4];
		int i = 0;
		if (cursor.moveToFirst()) {
			do {
				results[i][0] = cursor.getString(0); // 0 is the first column
				results[i][1] = cursor.getString(1); 
				results[i][2] = cursor.getString(2);
				results[i][3] = cursor.getString(3);
				i++;
			} while (cursor.moveToNext());
		}
		return results;
	}
	
	
	public void insertIntoRecent(String listen_url) {
		SQLiteDatabase database = this.getWritableDatabase();
		long unix_timestamp = System.currentTimeMillis()/1000;
		String query = "INSERT INTO recent (server_name, listen_url, bitrate, genre, unix_timestamp) " +
				"SELECT server_name, listen_url, bitrate, genre, '" +
				unix_timestamp +
				"' FROM stations WHERE listen_url='" +
				listen_url +
				"' LIMIT 1";
		database.execSQL(query);
	}
	
	public void deleteAllRecent() {
		String query = "DELETE FROM recent";
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL(query);
	}
	
	public boolean isFavourite(String listen_url) {
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT server_name FROM favourites WHERE listen_url='" +
				listen_url +
				"'", null);
		if (cursor.moveToFirst()) {
			if (cursor.getCount() > 0)
				return true;
		}
		return false;
	}
	
	public void insertIntoFavourites(String listen_url) {
		SQLiteDatabase database = this.getWritableDatabase();
		String query = "INSERT INTO favourites (server_name, listen_url, bitrate, genre) " +
				"SELECT server_name, listen_url, bitrate, genre FROM stations WHERE listen_url='" +
				listen_url +
				"' LIMIT 1";
		database.execSQL(query);
	}
	
	public void deleteFromFavourites(String listen_url) {
		SQLiteDatabase database = this.getWritableDatabase();
		String query = "DELETE FROM favourites WHERE listen_url='" +
				listen_url +
				"'";
		database.execSQL(query);
	}
	
	public void deleteAllFavourites() {
		String query = "DELETE FROM favourites";
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL(query);
	}
	
	public String[][] getStationsFavourites() {
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery("SELECT server_name, listen_url, bitrate,ROWID FROM favourites GROUP BY listen_url ORDER BY server_name", null);
		String[][] results = new String[cursor.getCount()][4];
		int i = 0;
		if (cursor.moveToFirst()) {
			do {
				results[i][0] = cursor.getString(0); // 0 is the first column
				results[i][1] = cursor.getString(1); 
				results[i][2] = cursor.getString(2);
				results[i][3] = cursor.getString(3);
				i++;
			} while (cursor.moveToNext());
		}
		return results;
	}
	
	public void insertManuallyStation(String server_name, String listen_url) {
		SQLiteDatabase database = this.getWritableDatabase();
		String query = "INSERT INTO favourites (server_name, listen_url, bitrate, genre) VALUES ('" +
			server_name +
			"','" +
			listen_url +
			"','0','manually_added')";
		database.execSQL(query);
	}
	
	public String[][] getStationsSearch(String query) {
		SQLiteDatabase database = this.getReadableDatabase();
		
		String query1 = "SELECT server_name, listen_url, bitrate, ROWID FROM stations WHERE (genre LIKE '%" +
				query +
				"%') OR (server_name LIKE '%" +
				query +
				"%')";
		Cursor cursor = database.rawQuery(query1, null);
		String[][] results = new String[cursor.getCount()][4];
		int i = 0;
		if (cursor.moveToFirst()) {
			do {
				results[i][0] = cursor.getString(0); // 0 is the first column
				results[i][1] = cursor.getString(1); 
				results[i][2] = cursor.getString(2);
				results[i][3] = cursor.getString(3);
				i++;
			} while (cursor.moveToNext());
		}
		return results;
	}

	public String getSetting(String name) {
		SQLiteDatabase database = this.getReadableDatabase();
		String query1 = "SELECT val FROM settings WHERE name='" + name + "'";
		Cursor cursor = database.rawQuery(query1, null);
		String result = null;
		if (cursor.moveToFirst()) {
				result = cursor.getString(0); // 0 is the first column   
		}
		return result;
	}
	
	public void setSetting(String name, String value) {
		SQLiteDatabase database = this.getWritableDatabase();
		String query = "UPDATE settings SET val='" + value + "' WHERE name='" + name + "'";
		database.execSQL(query);
	}

	public void editStation(String server_name, String listen_url, String id) {
		String query = "UPDATE stations SET server_name='" + server_name + "', listen_url='" + listen_url + "' WHERE ROWID=" + id;
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL(query);
	}

	public void editFavourite(String server_name, String listen_url, String id) {
		String query = "UPDATE favourites SET server_name='" + server_name + "', listen_url='" + listen_url + "' WHERE ROWID=" + id;
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL(query);
	}
}