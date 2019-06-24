package com.matthewericpeter.triptracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TripManager extends AppCompatActivity {
    List<Trip> trips = new ArrayList<>();
    Trip tripToSend = new Trip();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_manager);
        ReadTrips();

        if (trips != null) {
            for (int count = 0; count < trips.size(); count++) {
                addTripButton(trips.get(count));
            }
        }



    }

    @Override public void onBackPressed(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("TRIP", (Serializable) tripToSend);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void addTripButton(Trip t){
        LinearLayout ll = findViewById(R.id.layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //create button
        final Button btn = new Button(this);

        //give the button the waypoint as a tag*might not be necessary?*
        btn.setTag(t); //TODO:remove tag if we can just send waypoint object

        //text for waypoint, should show its actual name
        btn.setText(t.name);
        btn.setLayoutParams(params);
        final String btnText = String.valueOf(t.lat.get(0));
        //add button to linear layout
        ll.addView(btn);

        //create onClick Listener for button
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Trip t = (Trip) btn.getTag();

                AlertDialog.Builder builder = new AlertDialog.Builder(TripManager.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.trip_details_screen, null);

                builder.setView(dialogView);

                final AlertDialog dialog = builder.create();

                TextView tripName = dialogView.findViewById(R.id.tripName);
                TextView startTime = dialogView.findViewById(R.id.startTime);
                TextView endTime = dialogView.findViewById(R.id.endTime);
                TextView distanceText = dialogView.findViewById(R.id.distanceText);
                TextView speedText = dialogView.findViewById(R.id.speedText);

                tripName.setText(t.name);
                startTime.setText(String.valueOf(t.startTime));
                endTime.setText(String.valueOf(t.endTime));

                float[] results = new float[5];
                float distanceTraveled = 0;
                long timeElapsed = (t.endTime.getTime() - t.startTime.getTime())/(1000*60); //time in hours= / 3600000
                for (int i = 0; i<t.lat.size()-1;i++) {
                    Location.distanceBetween(t.lat.get(i),t.lng.get(i),t.lat.get(i+1),t.lng.get(i+1),results);
                    distanceTraveled += results[0];
                }
                distanceText.setText(String.format("%s meters", String.valueOf(distanceTraveled)));
                distanceTraveled = distanceTraveled; //distance in kilometers = /1000
                float speed = distanceTraveled/timeElapsed;
                speedText.setText(String.format("%.9f meters/minute \n(could be mph or knots/h)", speed));


                Button showButton = dialogView.findViewById(R.id.showButton);
                showButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tripToSend = t;
                        dialog.cancel();
                        Toast.makeText(TripManager.this, t.name + " added to the map.",
                                Toast.LENGTH_LONG).show();

                        onBackPressed();
                    }
                });


                dialog.show();
            }
        });
    }

    public void ReadTrips() {
        SharedPreferences appSharedPrefs = PreferenceManager .getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("trips", "");
        System.out.println(json);
        trips = gson.fromJson(json, new TypeToken<ArrayList<Trip>>(){}.getType());

    }

}
