package com.matthewericpeter.triptracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class GoogleMapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener {
    //code 97 is Pick a trip from the trip manager
    static final int PICK_TRIP_REQUEST = 97;
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
    //List<LatLng> trip = new ArrayList<>();
    List<Trip> trips = new ArrayList<>();
    List<Waypoint> localWaypoints = new ArrayList<>();
    List<Waypoint> displayWaypoints = new ArrayList<>();
    boolean inTrip = false;
    boolean autoMoveCamera = true;
    String tripName = "";
    //This location callback is the gravy of the app, it gets the location and adds markers to the map
    LocationCallback mLocationCallback = getmLocationCallback();
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference wayRef = rootRef.child("Waypoints");

    public LocationCallback getmLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                //this list always has a size of 1. Maybe it is returned as a list in case the location updates multiple times before this is called?
                List<Location> locationList = locationResult.getLocations();

                //If there is a new location
                if (locationList.size() > 0) {
                    final Button weatherButton = findViewById(R.id.weatherButton);
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    mLastLocation = location;
                    //Getting the LatLng from the location
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (inTrip) {
                        System.out.println(latLng.latitude + " " + latLng.longitude);
                        System.out.println(trips.get(trips.size() - 1).name);
                        trips.get(trips.size() - 1).lat.add(latLng.latitude);
                        trips.get(trips.size() - 1).lng.add(latLng.longitude);

                        //Setting up the marker that will be added to the map.
                        //These settings can be adjusted depending on any logic we want
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.dot));  //THIS LINE CHANGES THE MARKER TO THE DOT YOU SEE. ANY IMAGE CAN BE USED
                        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                    }
                    //move map camera , 16 is the zoom I am using. Smaller = further away.
                    if (autoMoveCamera)
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    if (weatherButton.getText().equals(" ")) {
                        GetWeather();
                    }
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFrag != null) {
            mapFrag.getMapAsync(this);
        }


        //Getting Buttons
        final Button menu = this.findViewById(R.id.menuButton);
        final Button addWaypoint = this.findViewById(R.id.addWayButton);
        final Button startTrip = this.findViewById(R.id.startButton);
        final Button tripManager = this.findViewById(R.id.tripsButton);
        final Button waypointManager = this.findViewById(R.id.waypointsButton);
        final Button reCenter = this.findViewById(R.id.reCenterButton);
        Button WButton = this.findViewById(R.id.weatherButton);

        reCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                autoMoveCamera = true;
                mLocationCallback = getmLocationCallback();
                reCenter.setVisibility(View.GONE);
            }
        });


        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when menu is clicked, display buttons. If clicked again, hide them
                if (addWaypoint.getVisibility() == View.GONE) {
                    menu.setText(getString(R.string.menu_close));
                    addWaypoint.setVisibility(View.VISIBLE);
                    startTrip.setVisibility(View.VISIBLE);
                    tripManager.setVisibility(View.VISIBLE);
                    waypointManager.setVisibility(View.VISIBLE);
                } else {
                    menu.setText(getString(R.string.menu_menu));
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
                            wayRef.push().setValue(w);
                        } else {
                            localWaypoints.add(w);
                            displayWaypoints.add(w);
                            WriteWaypoints(w);
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
                Trip trip = new Trip();
                AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapsActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_start_trip, null);

                builder.setView(dialogView);

                final AlertDialog dialog = builder.create();

                Button okButton = dialogView.findViewById(R.id.okButton);
                final TextView name = dialogView.findViewById(R.id.nameText);

                final Date currentTime = Calendar.getInstance().getTime();
                if (inTrip) {
                    //We are ending the trip here, so we need to store the list and the name in an object or something
                    trips.get(trips.size() - 1).endTime = currentTime;
                    trips.get(trips.size() - 1).endLat = mLastLocation.getLatitude();
                    trips.get(trips.size() - 1).endLng = mLastLocation.getLongitude();

                    inTrip = false;
                    mLocationCallback = getmLocationCallback();
                    startTrip.setText(getString(R.string.start_trip));
                    Toast.makeText(GoogleMapsActivity.this, tripName + " Ended", Toast.LENGTH_LONG).show();
                    Log.i("Trip Ended", name.getText().toString());

                    //ADDING THE TRIP TO A LIST OF TRIPS OR JSON OR SOMETHING
                    //AS OF RIGHT NOW WE JUST LOSE THE latLng'S

                    WriteTrips();

                    //clear the trip list so we can start a new trip without keeping the last trip
                    trip.lat.clear();
                    trip.lng.clear();
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
                                mLocationCallback = getmLocationCallback();
                                tripName = name.getText().toString();
                                startTrip.setText(getString(R.string.end_trip));

                                Trip trip = new Trip();
                                trip.name = tripName;
                                trip.startTime = currentTime;
                                trip.startLat = mLastLocation.getLatitude();
                                trip.startLng = mLastLocation.getLongitude();
                                trips.add(trip);
                                //This is debug stuff, still useful for the user to see that it was added though
                                Toast.makeText(GoogleMapsActivity.this, name.getText().toString() + " Started", Toast.LENGTH_LONG).show();
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
                intent.putExtra("LOCAL_LIST", (Serializable) localWaypoints);
                intent.putExtra("DISPLAY_LIST", (Serializable) displayWaypoints);
                startActivityForResult(intent, PICK_WAYPOINTS_REQUEST);
            }
        });

        tripManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(GoogleMapsActivity.this, TripManager.class);
                startActivityForResult(myIntent, PICK_TRIP_REQUEST);

            }
        });


        this.findViewById(R.id.weatherButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                advancedWeather(view);
            }
        });
    }

    private void GetWeather() {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + mLastLocation.getLatitude() + "&lon=" + mLastLocation.getLongitude() + "&appid=c4da64d57a1d34aca9cd2b60d7ee89a8&units=imperial";
        final Button WButton = this.findViewById(R.id.weatherButton);
        final Button SButton = this.findViewById(R.id.sunButton);
        Typeface weatherFont = Typeface.createFromAsset(getAssets(), "weather.ttf");
        SButton.setTypeface(weatherFont);

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String temp = String.valueOf(main_object.getDouble("temp"));

                    WButton.setText(String.format("%s°F", temp));

                    int iconID = object.getInt("id");
                    setWeatherIcon(iconID);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }

    private void advancedWeather(View view) {
        Intent intent = new Intent(this, WeatherActivity.class);
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + mLastLocation.getLatitude() + "&lon=" + mLastLocation.getLongitude() + "&appid=c4da64d57a1d34aca9cd2b60d7ee89a8&units=imperial";
        Bundle bundle = new Bundle();
        bundle.putString("WeatherURL", url);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void setWeatherIcon(int iconID){
        String icon = "";
        int ID = iconID / 100;

        if(iconID == 800){
            icon = getString(R.string.weather_sunny);
        }
        else{
            switch(ID){
                case 2 :
                    icon = getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getString(R.string.weather_drizzle);
                    break;
                case 7 :
                    icon = getString(R.string.weather_foggy);
                    break;
                case 8 :
                    icon = getString(R.string.weather_cloudy);
                    break;
                case 6 :
                    icon = getString(R.string.weather_snowy);
                    break;
                case 5 :
                    icon = getString(R.string.weather_rainy);
                    break;
            }
        }
        Button sunIcon = findViewById(R.id.sunButton);
        sunIcon.setText(icon);
    }

    public void AddWaypoints() {
        Log.i("@@@", "AddWaypoints Called");
        Log.i("@@@", Integer.toString(displayWaypoints.size()));
        for (int i = 0; i < displayWaypoints.size(); i++) {
            Waypoint w = displayWaypoints.get(i);
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(w.latitude, w.longitude);
            markerOptions.position(latLng);
            markerOptions.title(w.name);
            mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        }
    }

    public void WriteWaypoints(Waypoint w) {
        try {
            File path = getFilesDir();
            File file = new File(path, "waypointList.txt");
            try (FileOutputStream stream = new FileOutputStream(file, true)) {
                String info = w.name + "," + w.latitude + "," + w.longitude + ",";
                stream.write(info.getBytes());

                Log.i("@@@", "Writing File - " + info);
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void ReadWaypoints() {
        String ret = "";

        try {
            InputStream inputStream = openFileInput("waypointList.txt");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int size = inputStream.available();
                char[] buffer = new char[size];

                inputStreamReader.read(buffer);
                inputStream.close();
                ret = new String(buffer);
                String[] info = ret.split(",");
                Log.i("###", Integer.toString(info.length));
                for (int i = 0; i < info.length; i = i + 3) {
                    Waypoint w = new Waypoint(info[i], Double.parseDouble(info[i + 1]), Double.parseDouble(info[i + 2]));
                    displayWaypoints.add(w);
                    localWaypoints.add(w);
                }
                Log.i("@@@", ret);
                inputStreamReader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void WriteTrips() {
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        List<Trip> tripsToBeSaved = trips;


        String savedJson = appSharedPrefs.getString("trips", "");
        assert savedJson != null;
        if (savedJson.length() != 0) {
            List<Trip> savedTrips = gson.fromJson(savedJson, new TypeToken<ArrayList<Trip>>() {
            }.getType());
            tripsToBeSaved.addAll(savedTrips);
        }
        String json = gson.toJson(tripsToBeSaved); //tasks is an ArrayList instance variable
        prefsEditor.putString("trips", json);
        prefsEditor.commit();
        tripsToBeSaved.clear();
    }

    public void DeleteTrip(Trip t) {
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        List<Trip> totalTrips = trips;

        String savedJson = appSharedPrefs.getString("trips", "");
        assert savedJson != null;
        if (savedJson.length() != 0) {
            List<Trip> savedTrips = gson.fromJson(savedJson, new TypeToken<ArrayList<Trip>>() {
            }.getType());
            totalTrips.addAll(savedTrips);
        }

        for (Trip temp: trips){
            if (temp.lat == t.lat)
                trips.remove(temp);
        }

        totalTrips.remove(t);
        String json = gson.toJson(totalTrips);
        prefsEditor.putString("trips", json);
        prefsEditor.commit();
        totalTrips.clear();
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


    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            Button reCenter = this.findViewById(R.id.reCenterButton);
            autoMoveCamera = false;
            mLocationCallback = getmLocationCallback();
            reCenter.setVisibility(View.VISIBLE);
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
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.setOnCameraMoveStartedListener(this);

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
        // If request is cancelled, the result arrays are empty.
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGoogleMap.clear();
        if (requestCode == PICK_WAYPOINTS_REQUEST) {
            if (resultCode == RESULT_OK) {
                //waypointManager sent back a list of waypoints.. load them
                displayWaypoints = (List<Waypoint>) data.getSerializableExtra("DISPLAY_LIST");
                localWaypoints = (List<Waypoint>) data.getSerializableExtra("LOCAL_LIST");
                if (displayWaypoints != null) {
                    AddWaypoints();
                } else {
                    //Some error handling for a problem i was having earlier.. probably not necessary anymore
                    Toast.makeText(GoogleMapsActivity.this, "Empty Waypoints recieved...",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                //Error handling if the Waypoint manager doesn't close with a result
                //This would likely be because the activity crashed somehow
                Toast.makeText(GoogleMapsActivity.this, "Waypoints not recieved...",
                        Toast.LENGTH_LONG).show();
            }
        }
        /*
        PICK_TRIP_REQUEST code is defined up top as 97, this will be returned from the activity
        startActivityForResult call is in the tripManager Click Listener (line310), uncomment when ready
        to return result code see: WaypointActivity.java (lines:110-113)
        */
        if (requestCode == PICK_TRIP_REQUEST) {
            if (resultCode == RESULT_OK) {
                //trip manager returned with a trip, fetch and display
                Trip t = (Trip) data.getSerializableExtra("TRIP");
                if (t != null) {
                    boolean type = (boolean) data.getSerializableExtra("TYPE");
                    if (type) {
                        for (int i = 0; i < t.lat.size(); i++) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng latLng = new LatLng(t.lat.get(i), t.lng.get(i));
                            markerOptions.position(latLng);
                            //markerOptions.title(t.name);
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.dot));
                            mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                        }
                        LatLng latLng = new LatLng(t.lat.get(0), t.lng.get(0));
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    } else {
                        DeleteTrip(t);
                        Toast.makeText(GoogleMapsActivity.this, "Trip Deleted",
                                Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                //Error handling if the trip manager doesn't close with a result
                //This would likely be because the activity crashed somehow
                Toast.makeText(GoogleMapsActivity.this, "Trip not recieved..",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}


