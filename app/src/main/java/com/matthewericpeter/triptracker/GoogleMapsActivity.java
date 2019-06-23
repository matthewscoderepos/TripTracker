package com.matthewericpeter.triptracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class GoogleMapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    //code 98 is Pick waypoints from waypoint manager
    static final int PICK_WAYPOINTS_REQUEST = 98;
    // code 99 is ACCESS_FINE_LOCATION
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    List<LatLng> trip = new ArrayList<>();
    List<Waypoint> localWaypoints = new ArrayList<>();
    List<Waypoint> displayWaypoints = new ArrayList<>();
    boolean inTrip = false;
    boolean autoMoveCamera = true;
    String tripName = "";
    //This location callback is the gravy of the app, it gets the location and adds markers to the map
    LocationCallback mLocationCallback = getmLocationCallback();

    public LocationCallback getmLocationCallback() {
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                //this list always has a size of 1. Maybe it is returned as a list in case the location updates multiple times before this is called?
                List<Location> locationList = locationResult.getLocations();

                //If there is a new location
                if (locationList.size() > 0) {

                    //Debug, shows the size of the list that is returned

                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    mLastLocation = location;

//                //We could remove the markers, or set this if statement to a variable depending on if we are in a trip or not.
//                //This statement will remove the last marker that was placed
//                if (mCurrLocationMarker != null) {
//                    mCurrLocationMarker.remove();
//                }

                    //Getting the LatLng from the location
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (inTrip) {
                        /*
                         *
                         * ADDING THE latLng PAIRS TO THE LIST HERE
                         *
                         *
                         * */
                        trip.add(latLng);

                        //Setting up the marker that will be added to the map.
                        //These settings can be adjusted depending on any logic we want
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.dot));  //THIS LINE CHANGES THE MARKER TO THE DOT YOU SEE. ANY IMAGE CAN BE USED
                        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                    }
                    //move map camera , 18 is the zoom I am using. Smaller = further away.
                    if(autoMoveCamera)
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }
            }
        };
        return mLocationCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);


        // THIS CODE BLOCK PUTS THE "CENTER" BUTTON ON THE BOTTOM RIGHT
        View mapView = mapFrag.getView();
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    autoMoveCamera = true;
                    mLocationCallback = getmLocationCallback();
                }

            });
        }


        //Getting Buttons
        final Button menu = this.findViewById(R.id.menuButton);
        final Button addWaypoint = this.findViewById(R.id.addWayButton);
        final Button startTrip = this.findViewById(R.id.startButton);
        final Button tripManager = this.findViewById(R.id.tripsButton);
        final Button waypointManager = this.findViewById(R.id.waypointsButton);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when menu is clicked, display buttons. If clicked again, hide them
                if (addWaypoint.getVisibility() == View.GONE) {
                    menu.setText("Close");
                    addWaypoint.setVisibility(View.VISIBLE);
                    startTrip.setVisibility(View.VISIBLE);
                    tripManager.setVisibility(View.VISIBLE);
                    waypointManager.setVisibility(View.VISIBLE);
                } else {
                    menu.setText("Menu");
                    addWaypoint.setVisibility(View.GONE);
                    startTrip.setVisibility(View.GONE);
                    tripManager.setVisibility(View.GONE);
                    waypointManager.setVisibility(View.GONE);
                }
            }
        });

        addWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapsActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_add_waypoint, null);

                builder.setView(dialogView);

                final AlertDialog dialog = builder.create();

                final CheckBox publicBox = dialogView.findViewById(R.id.publicBox);
                Button okButton = dialogView.findViewById(R.id.okButton);
                final TextView name = dialogView.findViewById(R.id.nameText);

                okButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //When ok is clicked make a waypoint with the last location and given name, add it to the list of waypoints
                        Waypoint w = new Waypoint(name.getText().toString(), mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        localWaypoints.add(w);
                        displayWaypoints.add(w);
                            WriteWaypoints(w);

                        //This is debug stuff, still useful for the user to see that it was added though
                        Toast.makeText(GoogleMapsActivity.this, "Added this location as a waypoint", Toast.LENGTH_LONG).show();
                        Log.i("Location Added", name.getText().toString() + " " + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
                        //Make a new marker to the map at the current location
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        markerOptions.position(latLng);
                        markerOptions.title(name.getText().toString());
                        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                        if (publicBox.isChecked()) {
                            //ADD TO THE PUBLIC WAYPOINT TABLE HERE.
                        }
                        AddWaypoints();
                        //close the dialog box
                        dialog.cancel();
                    }
                });
                AddWaypoints();
                // Display the custom alert dialog on interface
                dialog.show();
            }

        });


        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapsActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_start_trip, null);

                builder.setView(dialogView);

                final AlertDialog dialog = builder.create();

                Button okButton = dialogView.findViewById(R.id.okButton);
                final TextView name = dialogView.findViewById(R.id.nameText);


                if (inTrip) {
                    //We are ending the trip here, so we need to store the list and the name in an object or something

                    inTrip = false;
                    startTrip.setText("Start Trip");
                    Toast.makeText(GoogleMapsActivity.this, tripName + " Ended", Toast.LENGTH_LONG).show();
                    Log.i("Trip Ended", name.getText().toString());

                    //ADDING THE TRIP TO A LIST OF TRIPS OR JSON OR SOMETHING
                    //AS OF RIGHT NOW WE JUST LOSE THE latLng'S

                    //clear the trip list so we can start a new trip without keeping the last trip
                    trip.clear();
                    //clear the map of all markers
                    mGoogleMap.clear();
                    //re-add the waypoint map markers
                    AddWaypoints();
                    dialog.cancel();

                } else {
                    //We are starting a trip here, so we need to get a name and set the inTrip bool to true
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!inTrip) {
                                //When ok is clicked start the trip, change inTrip to true
                                inTrip = true;
                                tripName = name.getText().toString();
                                startTrip.setText("End Trip");
                                //This is debug stuff, still useful for the user to see that it was added though
                                Toast.makeText(GoogleMapsActivity.this, name.getText().toString() + " Started", Toast.LENGTH_LONG).show();
                                Log.i("Trip Started", name.getText().toString());
                                dialog.cancel();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        });
        waypointManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapsActivity.this, WaypointActivity.class);
                intent.putExtra("LIST", (Serializable) localWaypoints);
                intent.putExtra("DISPLAY_LIST", (Serializable) displayWaypoints);
                startActivityForResult(intent, PICK_WAYPOINTS_REQUEST);
            }
        });

        tripManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(GoogleMapsActivity.this,
                        TripManager.class);
                startActivity(myIntent);
            }
        });


        this.findViewById(R.id.weatherButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                avancedWeather(view);
            }
        });
    }

    private void avancedWeather(View view) {
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
    }

    public void AddWaypoints(){
        Log.i("@@@", "AddWaypoints Called");
        Log.i("@@@", Integer.toString(displayWaypoints.size()));
        for(int i = 0; i < displayWaypoints.size(); i++){
            Waypoint w = displayWaypoints.get(i);
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(w.latitude, w.longitude);
            markerOptions.position(latLng);
            markerOptions.title(w.name);
            mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        }
    }

    public void WriteWaypoints(Waypoint w){
        try {
            File path = getFilesDir();
            File file = new File(path, "waypointList.txt");
            try (FileOutputStream stream = new FileOutputStream(file, true)) {
                String info = w.name + "," + w.latitude + "," + w.longitude + ",";
                stream.write(info.getBytes());

                Log.i("@@@", "Writing File - " + info);
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void ReadWaypoints(){
        String ret = "";

        try {
            InputStream inputStream = openFileInput("waypointList.txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int size = inputStream.available();
                char[] buffer = new char[size];

                inputStreamReader.read(buffer);
                inputStream.close();
                ret = new String(buffer);
                String[] info = ret.split(",");
                Log.i("###", Integer.toString(info.length));
                for(int i = 0;i<info.length;i = i+3) {
                    Waypoint w = new Waypoint(info[i], Double.parseDouble(info[i+1]), Double.parseDouble(info[i+2]));
                    displayWaypoints.add(w);
                    localWaypoints.add(w);
                }
                Log.i("@@@", ret);
                inputStreamReader.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocationCallback = getmLocationCallback();


        if (mFusedLocationClient == null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(1000);
                    mLocationRequest.setFastestInterval(1000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    //Location Permission already granted
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mGoogleMap.setMyLocationEnabled(true);
                } else {
                    //Request Location Permission
                    checkLocationPermission();
                }
            } else {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();

        mLocationCallback = getmLocationCallback();

        if (mFusedLocationClient == null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(1000);
                    mLocationRequest.setFastestInterval(1000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    //Location Permission already granted
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mGoogleMap.setMyLocationEnabled(true);
                } else {
                    //Request Location Permission
                    checkLocationPermission();
                }
            } else {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        //mGoogleMap.setMapType(GoogleMap.);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.setOnCameraMoveListener(this);

        //read from file and add all of the waypoints in the waypoint list
        ReadWaypoints();
        AddWaypoints();


        //checking for previously given permissions, then either getting location or asking for permissions
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }


    //testing this
    public void onCameraMove() {
        autoMoveCamera = false;
        mLocationCallback = getmLocationCallback();
    }

    //Android apps are an absolute COW when dealing with permissions. If you dont ask they crash, if you ask and they user says no, they crash unless you deal with it,
    //if you ask and the user says yes, too bad because you have to check every time you call the function anyway. These 2 functions check at first and then every time we need the location of the user
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(GoogleMapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        mGoogleMap.clear();
        if(requestCode == PICK_WAYPOINTS_REQUEST){
            if (resultCode == RESULT_OK){
                //waypointManager sent back a list of waypoints.. load them
                displayWaypoints = (List<Waypoint>) data.getSerializableExtra("LIST");
                if (displayWaypoints != null) {
                    AddWaypoints();
                }
                else {
                    Toast.makeText(GoogleMapsActivity.this, "Empty Waypoints recieved..",
                            Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(GoogleMapsActivity.this, "Waypoints not recieved..",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}


