package com.matthewericpeter.triptracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WaypointActivity extends AppCompatActivity {
    //Get Database reference for "Waypoints" (list of all waypoints)
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference wayRef = rootRef.child("Waypoints");
    List<Waypoint> localWaypoints = new ArrayList<Waypoint>();
    List<Waypoint> displayWaypoints = new ArrayList<Waypoint>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        localWaypoints = (List<Waypoint>) i.getSerializableExtra("LOCAL_LIST");
        displayWaypoints = (List<Waypoint>) i.getSerializableExtra("DISPLAY_LIST");

        setContentView(R.layout.activity_waypoint);

        FloatingActionButton addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WaypointActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.add_waypoint_manual, null);

                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();

                final CheckBox publicBox = dialogView.findViewById(R.id.publicBox);
                Button okButton = dialogView.findViewById(R.id.okButton);
                final TextView name = dialogView.findViewById(R.id.nameText);
                final TextView latitude = dialogView.findViewById(R.id.latText);
                final TextView longitude = dialogView.findViewById(R.id.longText);

                okButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //When ok is clicked make a waypoint with the last location and given name, add it to the list of waypoints
                        Double lat = null;
                        Double lng = null;
                        try{
                            lat = Double.parseDouble((latitude.getText().toString().trim()));
                        } catch(NumberFormatException e){
                            Toast.makeText(WaypointActivity.this, "Latitude is not a number.. try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                        try{
                            lng = Double.parseDouble((longitude.getText().toString().trim()));
                        } catch(NumberFormatException e){
                            Toast.makeText(WaypointActivity.this, "Longitude is not a number.. try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                        if (lat != null && lng != null) {
                            Waypoint w = new Waypoint(name.getText().toString(), lat, lng);

                            //This is debug stuff, still useful for the user to see that it was added though
                            Toast.makeText(WaypointActivity.this, "Added this location as a waypoint", Toast.LENGTH_LONG).show();
                            Log.i("Location Added", name.getText().toString() + " " + lat + " " +lng);
                            //Make a new marker to the map at the current location

                            if (publicBox.isChecked()) {
                                //ADD TO THE PUBLIC WAYPOINT TABLE HERE.
                                wayRef.push().setValue(w);
                            }
                            else{
                                localWaypoints.add(w);
                                displayWaypoints.add(w);
                                WriteWaypoints(w);
                                addWaypointButton(w);
                            }
                            //close the dialog box
                            dialog.cancel();
                        }
                    }
                });
                dialog.show();
            }
        });

        FloatingActionButton editBtn = findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: create edit menu
                //open edit menu, prompt user to select a Local waypoint to edit
                //pop the local waypoint, get changes, push new waypoint
                Toast.makeText(WaypointActivity.this, "EDIT WAYPOINT CLICKED",
                        Toast.LENGTH_LONG).show();
            }
        });
        //call function to create buttons from saved waypoints here
        for (int count = 0; count < localWaypoints.size(); count++) {
            addWaypointButton(localWaypoints.get(count));
        }

        wayRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                addWaypoints(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Tells us if a waypoint has been changed.
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //Tells us if a waypoint is removed.
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Tells us if a waypoint is moved.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Error catch if the database can't be loaded, probably should notify the user as this
                //would mostly be caused by a bad connection
                Toast.makeText(WaypointActivity.this, "Failed to connect to server, check connection and try again",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override public void onBackPressed(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("DISPLAY_LIST", (Serializable) displayWaypoints);
        returnIntent.putExtra("LOCAL_LIST", (Serializable) localWaypoints);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void addWaypoints(DataSnapshot dataSnapshot) {
        //get waypoint object from dataSnapshot
        final Waypoint newLoc = dataSnapshot.getValue(Waypoint.class);
        addWaypointButton(newLoc);
    }
    public void addWaypointButton(Waypoint w){
        LinearLayout ll = findViewById(R.id.layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //create button
        final Button btn = new Button(this);

        //give the button the waypoint as a tag*might not be necessary?*
        btn.setTag(w);

        //Check if waypoint is on the Display list, mark the button if so
        if (waypointDisplayed(w) != null) {
            String DisplayText = w.name + "\t(Displayed)";
            btn.setText(DisplayText);
        } else {
            btn.setText(w.name);
        }

        btn.setLayoutParams(params);
        //add button to linear layout
        ll.addView(btn);

        //create onClick Listener for button
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Waypoint w = (Waypoint)btn.getTag();
                Waypoint temp = waypointDisplayed(w);
                if(temp != null){
                    displayWaypoints.remove(temp);
                    btn.setText(w.name);
                    Toast.makeText(v.getContext(),"Removed waypoint: " + w.name,
                            Toast.LENGTH_LONG).show();
                }
                else{
                    displayWaypoints.add(w);
                    String DisplayText = w.name + "\t(Displayed)";
                    btn.setText(DisplayText);
                    Toast.makeText(v.getContext(),"Added waypoint: " + w.name,
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }
    public Waypoint waypointDisplayed(Waypoint w){
        for (Waypoint temp : displayWaypoints){
            if (temp.name.equals(w.name)){
                return temp;
            }
        }
        return null;
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
}
