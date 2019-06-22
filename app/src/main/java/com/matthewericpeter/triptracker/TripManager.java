package com.matthewericpeter.triptracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TripManager extends AppCompatActivity {
    List<Trip> trips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_manager);
        ReadTrips();

        for(int count = 0; count < trips.size(); count++){
            addTripButton(trips.get(count));
        }
    }



    public void addTripButton(Trip t){
        LinearLayout ll = findViewById(R.id.layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //create button
        final Button btn = new Button(this);

        //give the button the waypoint as a tag*might not be necessary?*
        //btn.setTag(w); //TODO:remove tag if we can just send waypoint object

        //text for waypoint, should show its actual name
        btn.setText(t.name);
        btn.setLayoutParams(params);
        final String btnText = t.name;
        //add button to linear layout
        ll.addView(btn);

        //create onClick Listener for button
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*TODO:Give button click an action
                when button is clicked, get its waypoint object and do something*
                 do something: send it to map or add it to a list to send later..*/
                //addWaypointToList(newLoc) ~pass waypoint to list
                //updateWaypointList(btn.getTag());
                Toast.makeText(v.getContext(),
                        "Button Clicked: " + btnText , Toast.LENGTH_LONG).show();
            }
        });
    }

    public void ReadTrips() {
        String ret;
        try {
            InputStream inputStream = openFileInput("tripList.txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int size = inputStream.available();
                char[] buffer = new char[size];

                inputStreamReader.read(buffer);
                inputStream.close();
                ret = new String(buffer);
                System.out.println(ret);
                String[] tripListString = ret.split("#");
                String[][] tripsInfoString = new String[tripListString.length][];
                for (int i = 0; i < tripListString.length; i++) {
                    Trip t = new Trip();
                    tripsInfoString[i] = tripListString[i].split(",");
                    t.name = tripsInfoString[i][0];
                    for (int j = 1; j < tripsInfoString[i].length; j = j+2){
                        LatLng latLng = new LatLng(Double.parseDouble(tripsInfoString[i][j]),Double.parseDouble(tripsInfoString[i][j+1]));
                        t.route.add(latLng);
                    }
                    trips.add(t);
                }

                inputStreamReader.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
