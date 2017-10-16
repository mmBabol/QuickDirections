package com.babol.android.quickdirections;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.app.AlertDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Database.DataSource;
import Database.MySQLiteHelper;
import ListView.*;

/*
*
* SimpleCursorAdapter - https://thinkandroid.wordpress.com/2010/01/09/simplecursoradapters-and-listviews/
*
* */

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // data source: an SQLitedatabase
    private DataSource datasource;

    // SimpleCursorAdapter
    private SimpleCursorAdapter adapter;

    private static final String MY_PREFS_NAME = "MyPrefsFile";
    private ListView listView;
    private List<LocationRow> rowItems;
    private ImageView imgCar;
    private ImageView imgBike;
    private ImageView imgWalk;
    private ImageView imgRemove;
    private LinearLayout modeTab;
    private TextView txtDelete;
    private boolean delLoc;
    private ProgressBar progressBar;

    private static final Integer[] image = {R.drawable.walking, R.drawable.bike,
            R.drawable.car, R.drawable.home, R.drawable.mountain};
    private static final Integer[] removeImg = {R.drawable.bin_closed, R.drawable.bin_open};
    private static final Integer[] imgOn = {R.drawable.car_green, R.drawable.bike_green, R.drawable.walk_green};
    private static final Integer[] imgOff = {R.drawable.car_black, R.drawable.bike_black, R.drawable.walk_black};
    private Integer imgPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgCar = (ImageView) findViewById(R.id.imgCar);
        imgBike = (ImageView) findViewById(R.id.imgBike);
        imgWalk = (ImageView) findViewById(R.id.imgWalk);
        imgRemove = (ImageView) findViewById(R.id.imgRemove);
        modeTab = (LinearLayout) findViewById(R.id.modeTab);
        txtDelete = (TextView) findViewById(R.id.txtDelete);
        delLoc = false;
        rowItems = new ArrayList<LocationRow>();



        datasource = new DataSource(this);
        datasource.open(); // create and open the database

        // retrieve all the comments from the database table
        Cursor cursor = datasource.getAllLocations();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        imgPref = prefs.getInt("mode", 0);
        onTransportMethod();

        String[] columns = new String[] {MySQLiteHelper.COLUMN_IMG, MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_ADDRESS};
        int[] to = new int[]{R.id.list_image, R.id.tvLocationName, R.id.tvStreet};

        adapter = new SimpleCursorAdapter(
                        this,
                        R.layout.list_items,
                        cursor,
                        columns ,
                        to /*, 0*/
                );

        listView = (ListView) findViewById(R.id.list_locations);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume(){
        datasource.open(); // open the database

        Cursor cursor = datasource.getAllLocations();
        adapter.changeCursor(cursor);
        super.onResume();
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        progressBar.setVisibility(View.VISIBLE);
        datasource.close(); // close the database
        delLoc = false;
        setImgRemove();
        Cursor cursor = adapter.getCursor();

        if (cursor != null)
            cursor.close(); // release the resources

        adapter.changeCursor(null); // the adapter has no Cursor

        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Open map with selected item address
        if(!delLoc){
            Intent data = new Intent(this, MapsActivity.class);
            Bundle bundle = new Bundle();
            LocationRow location = datasource.getLocation(id);

            bundle.putString("name", location.getLocationName());
            bundle.putDouble("latitude", location.getLatitude());
            bundle.putDouble("longitude", location.getLongitude());
            bundle.putString("address", location.toString());
            bundle.putInt("mode", imgPref);

            data.putExtras(bundle);
            startActivity(data);
        }
        // Delete selected address
        else{
            final long inside = id;
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Delete this location?");
            alertDialog.setMessage("Are you sure you want to delete this location?");
            alertDialog.setIcon(R.drawable.bin);

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Cursor cursor = null;
                            // remove the first comment from the database table
                            cursor = datasource.deleteLocation(inside);

                            // replace the adapter's Cursor
                            adapter.changeCursor(cursor);

                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public void onAdd(View view) throws IOException{
        startActivityForResult(new Intent(this, Add_Location.class), 1);
    }

    public void onDelete(View view) throws IOException{
        delLoc = !delLoc;
        setImgRemove();
    }

    private void setImgRemove(){
        if(delLoc) {
            imgRemove.setImageResource(removeImg[1]);
            modeTab.setVisibility(View.GONE);
            txtDelete.setVisibility(View.VISIBLE);
        }
        else {
            imgRemove.setImageResource(removeImg[0]);
            modeTab.setVisibility(View.VISIBLE);
            txtDelete.setVisibility(View.INVISIBLE);
        }
    }

    public void onTransportMethod() {
        switch(imgPref){
            // Bike
            case 1:
                imgCar.setImageResource(imgOff[0]);
                imgBike.setImageResource(imgOn[1]);
                imgWalk.setImageResource(imgOff[2]);
                break;
            // Walk
            case 2:
                imgCar.setImageResource(imgOff[0]);
                imgBike.setImageResource(imgOff[1]);
                imgWalk.setImageResource(imgOn[2]);
                break;
            // Car
            default:
                imgCar.setImageResource(imgOn[0]);
                imgBike.setImageResource(imgOff[1]);
                imgWalk.setImageResource(imgOff[2]);
        }
    }

    public void setMode0(View view){
        imgPref = 0;
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("mode", 0);
        editor.apply();
        onTransportMethod();
    }

    public void setMode1(View view){
        imgPref = 1;
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("mode", 0);
        editor.apply();
        onTransportMethod();
    }

    public void setMode2(View view){
        imgPref = 2;
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("mode", 0);
        editor.apply();
        onTransportMethod();
    }
}
