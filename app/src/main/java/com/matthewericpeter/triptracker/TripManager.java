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
    boolean type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_manager);
        Intent i = getIntent();
        List<Trip> sentTrips = (List<Trip>) i.getSerializableExtra("TRIPS");
        if (sentTrips != null)
            trips.addAll(sentTrips);
        ReadTrips();
        if (trips != null) {
            for (int count = 0; count < trips.size(); count++) {
                System.out.println("$$$$$$" + trips.get(count).name);
                addTripButton(trips.get(count));
            }
        }
    }

    @Override public void onBackPressed(){
        Intent returnIntent = new Intent();
        if (type) {
            if (tripToSend.endTime != null) {
                returnIntent.putExtra("TRIP", (Serializable) tripToSend);
                returnIntent.putExtra("TYPE", type);
                System.out.println("$$$$$$" + tripToSend.name);
            }
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }else{
            returnIntent.putExtra("TRIPS", (Serializable) trips);
            returnIntent.putExtra("TYPE", type);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    public void addTripButton(Trip t){
        final LinearLayout ll = findViewById(R.id.layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //create button
        final Button btn = new Button(this);

        //give the button the waypoint as a tag
        btn.setTag(t);

        //text for waypoint, should show its actual name
        btn.setText(t.name);
        btn.setLayoutParams(params);
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
                double distanceTraveled = 0;
                long timeElapsed = (t.endTime.getTime() - t.startTime.getTime());
                for (int i = 0; i<t.lat.size()-1;i++) {
                    Location.distanceBetween(t.lat.get(i),t.lng.get(i),t.lat.get(i+1),t.lng.get(i+1),results);
                    distanceTraveled += results[0];
                }
                distanceText.setText(String.format("%s meters", String.valueOf(distanceTraveled)));
                double speed = distanceTraveled/timeElapsed;
                speed = speed*2236.936; //meters/millisecond -> mph conversion
                speedText.setText(String.format("%.4s mph", speed));


                Button showButton = dialogView.findViewById(R.id.showButton);
                showButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tripToSend = t;
                        dialog.cancel();
                        Toast.makeText(TripManager.this, t.name + " added to the map.",
                                Toast.LENGTH_LONG).show();
                        btn.setText(String.format("%s\t(Displayed)", t.name));
                        type = true;
                    }
                });

                Button deleteButton = dialogView.findViewById(R.id.deleteButton);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteTrip(t);
                        dialog.cancel();
                        ll.removeView(btn);
                        Toast.makeText(TripManager.this, tripToSend.name + " deleted",
                                Toast.LENGTH_LONG).show();
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
        List<Trip> saved = gson.fromJson(json, new TypeToken<ArrayList<Trip>>(){}.getType());
        if (saved != null)
            trips.addAll(saved);
    }

    public void DeleteTrip(Trip t) {
        Gson gson = new Gson();
        Trip removeThis = new Trip();
        for (Trip temp: trips){
            if (temp.lat == t.lat && temp.name.equals(t.name))
                removeThis = temp;
        }
        trips.remove(removeThis);
        String json = gson.toJson(trips);
        WriteTrips();
    }

    public void WriteTrips() {
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trips);
        prefsEditor.putString("trips", json);
        prefsEditor.commit();
    }

}
