package com.matthewericpeter.triptracker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WaypointActivity extends AppCompatActivity {

    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference wayRef = rootRef.child("Waypoints");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waypoint);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final TextView txtView = findViewById(R.id.txtView);

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

                String _name = dataSnapshot.getKey().toString();
                //System.out.println("Name: " + _name);
                Waypoint newLoc = dataSnapshot.getValue(Waypoint.class);
                //System.out.println("Title: " + newLoc.name);
                System.out.println("Name: " + newLoc.name);
                System.out.println("Latitude: " + newLoc.latitude);
                System.out.println("Longitude: " + newLoc.longitude);

                // Waypoint newPt = new Waypoint(_name, newLoc);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //If a new Waypoint has been added, get it and add it to the list

                String _name = dataSnapshot.getKey().toString();
                System.out.println("Name: " + _name);
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
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart(){
        super.onStart();
        TextView txtView = findViewById(R.id.txtView);

    }

}
