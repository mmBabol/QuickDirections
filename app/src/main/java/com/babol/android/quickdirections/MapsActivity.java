package com.babol.android.quickdirections;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Directions.DirectionFinder;
import Directions.DirectionFinderListener;
import Directions.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DirectionFinderListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    ArrayList<String> directions;
    String locationName;
    double finalLatitude;
    double finalLongitude;
    String finalAddress;
    double currentLatitude;
    double currentLongitude;
    String currentAddress;
    Integer mode;
    LatLng startLatLng;
    LatLng endLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if Google play services are available, need for getting current location
        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_maps);

            // Place some default values
            /* Lat, Long
            *
            *  Seneca
            *  43.771620, -79.499616
            *
            *  Home
            *  43.636329, -79.404796
            */

            Intent intent = getIntent();
            locationName = intent.getStringExtra("name");
            finalLatitude = intent.getDoubleExtra("latitude", 1);
            finalLongitude = intent.getDoubleExtra("longitude", 1);
            finalAddress = intent.getStringExtra("address");
            mode = intent.getIntExtra("mode", 0);
            directions = new ArrayList<String>();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            initMap();
        } else
            Toast.makeText(this, "Error, google service not available!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(mGoogleApiClient!=null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        googleServicesAvailable();
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Add marker
        //LatLng destination = new LatLng(finalLatitude, finalLongitude);
        //mMap.addMarker(new MarkerOptions().position(destination).title(locationName));

        // To allow users their current location, use setMyLocationEnabled(true)
        // User needs to click the location button located at the top right of the map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        // adds current location icon on top right of map
        mMap.setMyLocationEnabled(true);
    }

    // Find users current location, find LatLng as well as address
    private void locateMe() throws IOException {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            currentLatitude = mLocation.getLatitude();
            currentLongitude = mLocation.getLongitude();

            Geocoder geocoder;
            List<Address> yourAddress;
            geocoder = new Geocoder(this, Locale.getDefault());
            yourAddress = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
            currentAddress = yourAddress.get(0).getAddressLine(0);
            String city;
            if(yourAddress.get(0).getLocality() != null)
                city = yourAddress.get(0).getLocality();
            else
                city = yourAddress.get(0).getSubLocality();
            currentAddress += ", " + city + ", " + yourAddress.get(0).getPostalCode();

            sendRequest();
        }
        else
            Toast.makeText(this, "Could not find the location! error 104", Toast.LENGTH_LONG).show();
    }

    // Change camera view including zoom
    // Use current and final lat/long points and calculate distance between both, animate camera zoom
    private void goToLocationZoom() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startLatLng);
        builder.include(endLatLng);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    // Check if google services is available
    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        /*
        * 0 - successful
        * 1 - failed
        * 2 - failed, but able to install, send to google play service download
        * */
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS)
            return true;
        else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else
            Toast.makeText(this, "Can't connect to play services!", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Once connected with google api, get the location
        try {
            locateMe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void sendRequest() {
        try {
            new DirectionFinder(this, currentAddress, finalAddress, mode).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }
        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start2))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end2))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(0xa900a900).
                    width(15);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            directions = route.directions;

            polylinePaths.add(mMap.addPolyline(polylineOptions));

            startLatLng = route.startLocation;
            endLatLng = route.endLocation;

            goToLocationZoom();
        }
    }

    // Open Google maps and start navigations
    public void navigateMe(View view) {
        char modeChar;
        switch(mode){
            case 1:
                modeChar = 'b';
                break;
            case 2:
                modeChar = 'w';
                break;
            default:
                modeChar = 'd';
        }

        //google.navigation:q=latitude,longitude
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + finalLatitude + "," + finalLongitude + "&mode=" + modeChar);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    // Open directions activity and pass ArrayList of directions to it
    public void showDirections(View view){
        Intent mIntent = new Intent(this, DirectionsActivity.class);
        mIntent.putStringArrayListExtra("key", directions);
        startActivity(mIntent);
    }
}