package com.babol.android.quickdirections;


import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import Database.DataSource;


/*
*
* Place autocomplete: https://developers.google.com/places/android-api/autocomplete
*
* */

public class Add_Location extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // data source: an SQLitedatabase
    private DataSource datasource;

    private static final String TAG = "Error";
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    private TextView nameTV;
    private PlaceAutocompleteFragment autocompleteFragment;
    private static final Integer[] image = {R.drawable.walking, R.drawable.bike,
            R.drawable.car, R.drawable.home, R.drawable.mountain};

    private String address;
    private String city;
    private String postal;
    private double lat;
    private double lng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location);

        datasource = new DataSource(this);
        datasource.open(); // create and open the database

        nameTV = (TextView) findViewById(R.id.et_Name);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                LatLng latlng = place.getLatLng();
                lat = latlng.latitude;
                lng = latlng.longitude;
                try {
                    setAddress();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        try {
            onLocateMe();
        } catch (IOException ex){
            Toast.makeText(this, "Error getting current location.", Toast.LENGTH_LONG).show();
        }
    }

    public void onLocateMe(View view) throws IOException {
        onLocateMe();
    }

    // Find current location, find current LatLng then use Geocoder to find full address
    public void onLocateMe() throws IOException {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            lat = mLocation.getLatitude();
            lng = mLocation.getLongitude();
            setAddress();
            autocompleteFragment.setText(address + ", " + city);
        }
        else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    // Get entire address  from lat/long points
    private void setAddress() throws IOException {
        Geocoder geocoder;
        List<Address> yourAddress;
        geocoder = new Geocoder(this, Locale.getDefault());
        yourAddress = geocoder.getFromLocation(lat, lng, 1);
        address = yourAddress.get(0).getAddressLine(0);
        if(yourAddress.get(0).getLocality() != null)
            city = yourAddress.get(0).getLocality();
        else
        city = yourAddress.get(0).getSubLocality();
        postal = yourAddress.get(0).getPostalCode();
    }

    // Save results, put all data into bundle to send back
    public void onSave(View view) throws IOException {
        String name_value = nameTV.getText().toString();
        String address_value = address + ", " + city + ", " + postal;

        if (!name_value.isEmpty() && !address_value.isEmpty()) {
            Intent data = new Intent();
            Random rn = new Random();
            int imageID = rn.nextInt(4);

            Cursor cursor = null;
            // save the new comment (string) to the database table
            cursor = datasource.addLocation(image[imageID], name_value, lat, lng,
                    address, city, postal);

            setResult(RESULT_OK, data);
            finish();

        } else
            Toast.makeText(this, "Cannot leave fields blank!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            lat = mLocation.getLatitude();
            lng = mLocation.getLongitude();
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
