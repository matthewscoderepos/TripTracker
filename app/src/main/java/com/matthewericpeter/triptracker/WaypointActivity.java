package com.matthewericpeter.triptracker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WaypointActivity extends AppCompatActivity {

    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference wayRef = rootRef.child("Waypoints");

    //LayoutInflater inflater = getLayoutInflater();
    //View v = inflater.inflate(R.layout.activity_waypoint, null);





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        wayRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Child Added is called for each object on startup.. we can use this to get them for the list

                /*String _name = dataSnapshot.getKey().toString();
                System.out.println("ADD CHILD LISTENER ");
                //create method to
                Waypoint newLoc = dataSnapshot.getValue(Waypoint.class);
                //System.out.println("Title: " + newLoc.name);
                System.out.println("Name: " + newLoc.name);
                System.out.println("Latitude: " + newLoc.latitude);
                System.out.println("Longitude: " + newLoc.longitude);*/

                // Waypoint newPt = new Waypoint(_name, newLoc);
                addWaypoint(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //If a new Waypoint has been changed, get the changes and update

                String _name = dataSnapshot.getKey().toString();
                System.out.println("CHANGE CHILD LISTENER ");
                Waypoint newLoc = dataSnapshot.getValue(Waypoint.class);
                //System.out.println("Title: " + newLoc.name);
                System.out.println("Name: " + newLoc.name);
                System.out.println("Latitude: " + newLoc.latitude);
                System.out.println("Longitude: " + newLoc.longitude);

                // Waypoint newPt = new Waypoint(_name, newLoc);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //Tells us if a waypoint is removed. We probably wont need this outside of testing

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Tells us if a waypoint is moved. We probably wont need this outside of testing
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Error catch if the database can't be loaded, probably should notify the user as this
                //would mostly be caused by a bad connection
                Toast.makeText(WaypointActivity.this, "Failed to connect to server, check connection and try again", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();

    }

    public void addWaypoint(DataSnapshot dataSnapshot){
        ScrollView waypointView = findViewById(R.id.waypointView);

        LinearLayout ll = findViewById(R.id.layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        //get waypoint object from dataSnapshot
        Waypoint newLoc = dataSnapshot.getValue(Waypoint.class);

        /*print testing:
        System.out.println("Name: " + newLoc.name);
        System.out.println("Latitude: " + newLoc.latitude);
        System.out.println("Longitude: " + newLoc.longitude);*/

        //create button
        Button btn = new Button(this);
        //give the button the waypoint as a tag*might not be necessary?*
        btn.setTag(newLoc);
        //text for waypoint, should show its actual name
        btn.setText(newLoc.name);
        btn.setLayoutParams(params);
        //add button to linear layout
        ll.addView(btn);

        //create onClick Listener for button
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //***TODO:Give button click an action
                //when button is clicked, get its waypoint object and do something*
                //  *do something: send it to map or add it to a list to send later..

                Toast.makeText(v.getContext(),
                        "Button Clicked: ", Toast.LENGTH_LONG).show();
            }
        });
    }

}
