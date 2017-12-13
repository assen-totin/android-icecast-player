package com.voody.icecast.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by assen.totin on 13/12/17.
 */

public class FetchStations extends Activity {
    private int mode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init DB
        SQLiteHelper dbHelper = new SQLiteHelper(FetchStations.this);
        dbHelper.deleteFromStations();
        dbHelper.insertIntoUpdates();

        Bundle dnld1 = new Bundle();
        dnld1.putString("download_name", "Icecast");
        dnld1.putString("file_url", "http://dir.xiph.org/yp.xml");
        dnld1.putString("file_name", "yp.xml");
        dnld1.putString("separator", " ");
        dnld1.putInt("mode", 1);

        Intent intent1 = new Intent(FetchStations.this, DownloadFile.class);
        intent1.putExtras(dnld1);
        startActivity(intent1);

        Bundle dnld2 = new Bundle();
        dnld2.putString("download_name", "Radio Browser");
        dnld2.putString("file_url", "http://www.radio-browser.info/webservice/xml/stations");
        dnld2.putString("file_name", "stations");
        dnld2.putString("separator", ",");
        dnld2.putInt("mode", 2);

        Intent intent2 = new Intent(FetchStations.this, DownloadFile.class);
        intent2.putExtras(dnld2);
        startActivity(intent2);

        finish();
    }
}
