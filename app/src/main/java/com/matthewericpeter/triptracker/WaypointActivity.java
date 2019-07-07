package com.matthewericpeter.triptracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WaypointActivity extends AppCompatActivity {
    //Get Database reference for "Waypoints" (list of all waypoints)
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference wayRef = rootRef.child("Waypoints");
    List<Waypoint> localWaypoints = new ArrayList<Waypoint>();
    List<Waypoint> displayWaypoints = new ArrayList<Waypoint>();
    List<Waypoint> publicWaypoints = new ArrayList<Waypoint>();
    Double currentLatitude;
    Double currentLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        localWaypoints = (List<Waypoint>) i.getSerializableExtra("LOCAL_LIST");
        displayWaypoints = (List<Waypoint>) i.getSerializableExtra("DISPLAY_LIST");
        currentLatitude = i.getDoubleExtra("CURRENT_LAT", 0);
        currentLongitude = i.getDoubleExtra("CURRENT_LNG", 0);


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
                final CheckBox currentLocationBox = dialogView.findViewById(R.id.currentLocationBox);
                Button okButton = dialogView.findViewById(R.id.okButton);
                final EditText name = dialogView.findViewById(R.id.nameText);
                final EditText latitude = dialogView.findViewById(R.id.latText);
                final EditText longitude = dialogView.findViewById(R.id.longText);
                final TextView currentLat = dialogView.findViewById(R.id.currentLat);
                final TextView currentLong = dialogView.findViewById(R.id.currentLong);

                currentLocationBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            String curLat = currentLatitude.toString();
                            String curLng = currentLongitude.toString();
                            latitude.setVisibility(View.GONE);
                            longitude.setVisibility(View.GONE);
                            currentLat.setText(curLat);
                            currentLong.setText(curLng);
                            currentLat.setVisibility(View.VISIBLE);
                            currentLong.setVisibility(View.VISIBLE);

                        }
                        else{
                            latitude.setVisibility(View.VISIBLE);
                            longitude.setVisibility(View.VISIBLE);
                            currentLat.setVisibility(View.GONE);
                            currentLong.setVisibility(View.GONE);
                        }
                    }
                });
                okButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //When ok is clicked make a waypoint with the last location and given name, add it to the list of waypoints
                        Double lat = null;
                        Double lng = null;
                        if (currentLocationBox.isChecked()){
                            lat = currentLatitude;
                            lng = currentLongitude;

                        }
                        else {
                            try {
                                lat = Double.parseDouble((latitude.getText().toString().trim()));
                            } catch (NumberFormatException e) {
                                Toast.makeText(WaypointActivity.this, "Latitude is not a number.. try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                            try {
                                lng = Double.parseDouble((longitude.getText().toString().trim()));
                            } catch (NumberFormatException e) {
                                Toast.makeText(WaypointActivity.this, "Longitude is not a number.. try again.",
                                        Toast.LENGTH_LONG).show();
                            }
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
                                addWaypointButton(w);
                            }
                            else{
                                localWaypoints.add(w);
                                displayWaypoints.add(w);
                                WriteWaypoints(w);
                                addLocalWaypointButtons(w);
                            }
                            //close the dialog box
                            dialog.cancel();
                        }
                    }
                });
                dialog.show();
            }
        });

        wayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sortPublicWaypoints(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Error catch if the database can't be loaded, probably should notify the user as this
                //would mostly be caused by a bad connection
                Toast.makeText(WaypointActivity.this, "Failed to connect to server, check connection and try again",
                        Toast.LENGTH_LONG).show();
            }
        });

        sortLocalWaypoints();
    }

    @Override public void onBackPressed(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("DISPLAY_LIST", (Serializable) displayWaypoints);
        returnIntent.putExtra("LOCAL_LIST", (Serializable) localWaypoints);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void addWaypointButton(Waypoint w){
        //Layout Parameters to be used by layouts & buttons
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout publicLayout = findViewById(R.id.publicLayout);

        //create & add button
        final Button btn = new Button(this);
        btn.setTag(w);
        btn.setLayoutParams(params);
        publicLayout.addView(btn);

        //Check if waypoint is on the Display list, mark the button if so
        if (waypointDisplayed(w) != null) {
            String DisplayText = w.name + "\t(Displayed)";
            btn.setText(DisplayText);
        } else {
            btn.setText(w.name);
        }

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
    public void addLocalWaypointButtons(Waypoint w){
        //Layout Parameters to be used by layouts & buttons
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f);
        LinearLayout.LayoutParams altBtnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0f);

        //Create childLayout (to hold horizontal buttons) and get localLayout (to hold list of all local buttons)
        final LinearLayout childLayout = new LinearLayout(this);
        final LinearLayout localLayout = findViewById(R.id.localLayout);

        childLayout.setLayoutParams(params);
        childLayout.setOrientation(LinearLayout.HORIZONTAL);

        //create buttons
        final Button btn = new Button(this);
        btn.setTag(w);
        btn.setLayoutParams(btnParams);

        final ImageButton delBtn = new ImageButton (this);
        String delUri = "@android:drawable/ic_menu_delete";
        int delImageResource = getResources().getIdentifier(delUri, null, getPackageName());
        delBtn.setImageResource(delImageResource);
        delBtn.setLayoutParams(altBtnParams);

        final ImageButton editBtn = new ImageButton(this);
        String editUri = "@android:drawable/ic_menu_edit";
        int editImageResource = getResources().getIdentifier(editUri, null, getPackageName());
        editBtn.setImageResource(editImageResource);
        editBtn.setLayoutParams(altBtnParams);

        //add button to linear layout
        childLayout.addView(btn);
        childLayout.addView(delBtn);
        childLayout.addView(editBtn);
        localLayout.addView(childLayout);

        //Check if waypoint is on the Display list, mark the button if so
        if (waypointDisplayed(w) != null) {
            String DisplayText = w.name + "\t(Displayed)";
            btn.setText(DisplayText);
        } else {
            btn.setText(w.name);
        }

        //create onClick Listener for display button
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
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Waypoint w = (Waypoint)btn.getTag();
                Waypoint temp = getWaypointLocal(w);
                //edit button has been clicked.. get waypoint info and display to be edited
                AlertDialog.Builder builder = new AlertDialog.Builder(WaypointActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.edit_local_waypont, null);
                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();

                final EditText nameEdit = dialogView.findViewById(R.id.nameEdit);
                nameEdit.setText(w.name);

                final EditText latEdit = dialogView.findViewById(R.id.latEdit);
                latEdit.setText(String.valueOf(w.latitude));

                final EditText lngEdit = dialogView.findViewById(R.id.lngEdit);
                lngEdit.setText(String.valueOf(w.longitude));
                final CheckBox makePublicBox = dialogView.findViewById(R.id.makePublicBox);
                Button saveBtn = dialogView.findViewById(R.id.saveBtn);
                Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
                final TextView publicNote = dialogView.findViewById(R.id.publicNoteText);

                makePublicBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            //Display notice here
                            publicNote.setVisibility(View.VISIBLE);
                        }
                        else{
                            //Hide notice here
                            publicNote.setVisibility(View.GONE);
                        }
                    }
                });

                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Double newLat = null;
                        Double newlng = null;
                        try{
                            newLat = Double.parseDouble((latEdit.getText().toString().trim()));
                        } catch(NumberFormatException e){
                            Toast.makeText(WaypointActivity.this, "Latitude is not a number.. try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                        try{
                            newlng = Double.parseDouble((lngEdit.getText().toString().trim()));
                        } catch(NumberFormatException e){
                            Toast.makeText(WaypointActivity.this, "Longitude is not a number.. try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                        if (newLat != null && newlng != null) {
                            Waypoint newW = new Waypoint(nameEdit.getText().toString(), newLat, newlng);

                            //This is debug stuff, still useful for the user to see that it was added though
                            Log.i("Location Edited", nameEdit.getText().toString() + " " + newLat + " " +newlng);
                            //Make a new marker to the map at the current location

                            if (makePublicBox.isChecked()) {
                                //Add to the Database and remove from local waypoints
                                deleteWaypoint(w);
                                localLayout.removeView(childLayout);

                                wayRef.push().setValue(w);
                            }
                            else{
                                editWaypoint(w, newW);
                            }
                        Toast.makeText(v.getContext(),"Editing waypoint: " + w.name,
                                Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        }
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Waypoint w = (Waypoint)btn.getTag();

                //delete button has been clicked.. get waypoint info and display confirmation
                AlertDialog.Builder builder = new AlertDialog.Builder(WaypointActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.del_waypoint_confirm, null);
                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();

                TextView waypointNameView = dialogView.findViewById(R.id.waypointNameView);
                TextView waypointLatView = dialogView.findViewById(R.id.waypointLatView);
                TextView waypointLongView = dialogView.findViewById(R.id.waypointLongView);
                Button yesBtn = dialogView.findViewById(R.id.yesBtn);
                Button noBtn = dialogView.findViewById(R.id.noBtn);


                waypointNameView.setText(w.name);
                String latText = "Latitude: " + w.latitude;
                String lngText = "Longitude: " + w.longitude;
                waypointLatView.setText(latText);
                waypointLongView.setText(lngText);

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteWaypoint(w);
                        Toast.makeText(v.getContext(),"Deleted waypoint: " + w.name,
                                Toast.LENGTH_LONG).show();
                        localLayout.removeView(childLayout);
                        dialog.cancel();
                    }
                });
                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.cancel();
                    }
                });
                dialog.show();

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
    public Waypoint getWaypointLocal(Waypoint w){
        for (Waypoint temp : localWaypoints){
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
    public void deleteWaypoint(Waypoint w){
        //send waypoint to be deleted here, rewrite local file waypointList.txt to delete waypoint
        Waypoint localTemp = getWaypointLocal(w);
        Waypoint dispTemp = waypointDisplayed(w);

        localWaypoints.remove(localTemp);
        displayWaypoints.remove(dispTemp);


        try {
            File path = getFilesDir();
            File oldFile = new File(path, "waypointList.txt");

            if(!oldFile.delete()){
                Log.i("@@@@", "Could not delete existing file");
            }

            File newFile = new File(path, "waypointList.txt");
            try (FileOutputStream stream = new FileOutputStream(newFile, true)) {
                for (int count = 0; count < localWaypoints.size(); count++) {
                    Waypoint temp = localWaypoints.get(count);
                    String info = temp.name + "," + temp.latitude + "," + temp.longitude + ",";
                    stream.write(info.getBytes());

                    Log.i("@@@", "Writing File - " + info);
                }
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    public void editWaypoint(Waypoint oldW, Waypoint newW){
        //send waypoint to be deleted here, rewrite local file waypointList.txt to delete waypoint
        Waypoint localTemp = getWaypointLocal(oldW);
        Waypoint dispTemp = waypointDisplayed(oldW);

        localWaypoints.remove(localTemp);
        displayWaypoints.remove(dispTemp);
        localWaypoints.add(newW);
        displayWaypoints.add(newW);

        try {
            File path = getFilesDir();
            File oldFile = new File(path, "waypointList.txt");

            if(!oldFile.delete()){
                Log.i("@@@@", "Could not delete existing file");
            }

            File newFile = new File(path, "waypointList.txt");
            try (FileOutputStream stream = new FileOutputStream(newFile, true)) {
                for (int count = 0; count < localWaypoints.size(); count++) {
                    Waypoint temp = localWaypoints.get(count);
                    String info = temp.name + "," + temp.latitude + "," + temp.longitude + ",";
                    stream.write(info.getBytes());

                    Log.i("@@@", "Writing File - " + info);
                }
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    public void sortLocalWaypoints(){
        List<WaypointAndDistance> localDistances = new ArrayList<WaypointAndDistance>();

        for (Waypoint temp : localWaypoints){
            WaypointAndDistance distTemp = new WaypointAndDistance(temp, currentLatitude, currentLongitude);
            localDistances.add(distTemp);
        }
        localWaypoints.clear();
        Collections.sort(localDistances,new DistanceComparator());

        for (WaypointAndDistance temp : localDistances){
            localWaypoints.add(temp.waypoint);
            addLocalWaypointButtons(temp.waypoint);
        }
    }
    public void sortPublicWaypoints(DataSnapshot dataSnapshot){
        List<WaypointAndDistance> publicDistances = new ArrayList<WaypointAndDistance>();
        for(DataSnapshot data : dataSnapshot.getChildren()) {
            final Waypoint temp = data.getValue(Waypoint.class);
            WaypointAndDistance distTemp = new WaypointAndDistance(temp, currentLatitude, currentLongitude);
            publicDistances.add(distTemp);
        }
        Collections.sort(publicDistances,new DistanceComparator());
        for (WaypointAndDistance temp : publicDistances){
            publicWaypoints.add(temp.waypoint);
            addWaypointButton(temp.waypoint);
            Log.i("***",temp.waypoint.name);
        }
    }
    public class DistanceComparator implements Comparator<WaypointAndDistance>{
        public int compare(WaypointAndDistance w1, WaypointAndDistance w2){
            float dist1 = w1.distance;
            float dist2 = w2.distance;

            return (int)(dist1-dist2);
        }
    }
}
