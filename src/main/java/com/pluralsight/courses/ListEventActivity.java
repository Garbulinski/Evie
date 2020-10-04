package com.pluralsight.courses;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pluralsight.courses.maps.MapsActivity;
import com.pluralsight.courses.users.Event;
import com.pluralsight.courses.users.Image;
import com.pluralsight.courses.users.Location;
import com.pluralsight.courses.utility.MyAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ListEventActivity extends AppCompatActivity {

    private static final String TAG = "SignedInActivity";

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    // widgets and UI References
    FloatingActionButton floatingActionButton;
    BottomNavigationView bottomAppBar;
    private ListView mListView;
    private ArrayList<Event> events;
    private MyAdapter mAdapter;
    private static final int ERROR_DIALOG_REQUEST = 90001;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signedin);
        Log.d(TAG, "onCreate: started.");
        getSupportActionBar().setTitle("Public Events");
        setupFirebaseAuth();
        floatingActionButton = findViewById(R.id.fab);
        bottomAppBar = findViewById(R.id.bottomBar);
        bottomAppBar.setSelectedItemId(R.id.home);
        mListView = (ListView) findViewById(R.id.listView);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3 = new Intent(ListEventActivity.this, AddEventActivty.class);
                startActivity(intent3);
            }
        });
        bottomAppBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.optionSignOut:
//
                        Intent intent4 = new Intent(ListEventActivity.this,ShowScheduledEventActivity.class);
                        startActivity(intent4);
                        return true;
                    case R.id.optionAccountSettings:
                        Intent intent = new Intent(ListEventActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.maps:
                        Intent intent1 = new Intent(ListEventActivity.this, MapsActivity.class);
                        startActivity(intent1);
                        return true;
//                    case R.id.home:
//                        Intent intent3 = new Intent(SignedInActivity.tActivity.class);
//                        startActivity(intent3);
//                        return true;
                    case R.id.attend:
                        Intent intent5 = new Intent(getApplicationContext(), QrScannerActivity.class);
                        startActivity(intent5);
                        return true;
                    default:
                        return false;
                }

            }
        });
        init();
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        bottomAppBar.getMenu().getItem(2).setChecked(true);
        checkAuthenticationState();
    }
    public void init(){
        getEvents();
    }

    private void getEvents() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child("events");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                events = new ArrayList<Event>();
                for(DataSnapshot singleSnapshot:  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found event: "
                            + singleSnapshot.getValue());

                    Event event = new Event();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        event.setEvent_id(objectMap.get("event_id").toString());
                    event.setEvent_name(objectMap.get("event_name").toString());
                    event.setEvent_image_description(objectMap.get("event_image_description").toString());
                    event.setEvent_description(objectMap.get("event_description").toString());
                    event.setEvent_uid(objectMap.get("event_uid").toString());
                    Location location = new Location();
                    location.setLatitude((Double) singleSnapshot.child("location").child("latitude").getValue());
                    location.setLongitude((Double) singleSnapshot.child("location").child("longitude").getValue());
                    event.setLocation(location);


                    ArrayList<Image> imagesList = new ArrayList<Image>();
                    for(DataSnapshot snapshot: singleSnapshot
                            .child("images").getChildren()){
                        Image image = new Image();
                        Map<String, Object> obj = (HashMap<String, Object>) snapshot.getValue();
                       image.setImage_uid(obj.get("image_uid").toString());
                       image.setImage_id(obj.get("image_id").toString());
                       image.setImage_uri(obj.get("image_uri").toString());
                       ArrayList<String> appreciations = new ArrayList<>();
                        for(DataSnapshot snap : snapshot.child("apreciations").getChildren())
                        {
                            String string= new String();
                            string = snap.getValue(String.class);
                            appreciations.add(string);
                        }
                        image.setApreciations(appreciations);
                       imagesList.add(image);
                    }
                    ArrayList<String> users = new ArrayList<>();
                    for (DataSnapshot snapishot: singleSnapshot.child("users").getChildren()){
                        String user = snapishot.getValue().toString();
                        users.add(user);
                    }
                    event.setImages(imagesList);
                    event.setUsers(users);
                    if(event.getEvent_uid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    || event.getUsers().contains(FirebaseAuth.getInstance().getCurrentUser().getUid() )){
                        events.add(event);
                    }
                }
          setupEventList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ListEventActivity.this, databaseError.toString()+"rada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showDeletEventDialog(String eventId){
        DeleteEventDialog dialog = new DeleteEventDialog();
        Bundle args = new Bundle();
        args.putString(getString(R.string.event_id_field), eventId);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_delete_event));
    }
    private void setupEventList(){
        Collections.sort(events,Event.AppreciationsCompare);
        mAdapter = new MyAdapter(ListEventActivity.this, R.layout.layout_listevents, events);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ListEventActivity.this, EventActivity.class);
                intent.putExtra(getString(R.string.intent_event), events.get(i).getEvent_id());
                startActivity(intent);
            }
        });

    }

    private void checkAuthenticationState() {
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(ListEventActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }


    /**
     * Sign out the current user
     */
    private void signOut(){
        Log.d(TAG, "signOut: signing out");
        FirebaseAuth.getInstance().signOut();
    }

    /*
            ----------------------------- Firebase setup ---------------------------------
         */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

//                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ListEventActivity.this, LoginActivity.class);
                     finish();
                }
                // ...
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }



}
