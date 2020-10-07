package com.pluralsight.courses;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

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
import com.pluralsight.courses.users.ScheduledEvent;
import com.pluralsight.courses.utility.ScheduledEventAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShowScheduledEventActivity extends AppCompatActivity {
    private static final String TAG = "ScheduledEventActivity";


    private ListView mListView;
    private ArrayList<ScheduledEvent> events;
    private ScheduledEventAdapter schedulesAdapter;
    BottomNavigationView bottomAppBar;

    FloatingActionButton floatingActionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_scheduled_event);
        bottomAppBar = findViewById(R.id.bottomBar);
        bottomAppBar.setSelectedItemId(R.id.optionSignOut);
        mListView = (ListView) findViewById(R.id.scheduledlistView);
        events = new ArrayList<ScheduledEvent>();
        getSupportActionBar().setTitle("Scheduled Events");

       DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("scheduled_events");
       reference.addValueEventListener(mValueEventListener);
        floatingActionButton = findViewById(R.id.addScheduledEvent);
        floatingActionButton.setVisibility(View.GONE);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowScheduledEventActivity.this,ScheduleEventActivity.class);
                startActivity(intent);
            }
        });
        bottomAppBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.optionSignOut:
//                        signOut();
                        Intent intent4 = new Intent(getApplicationContext(),ShowScheduledEventActivity.class);
                        startActivity(intent4);
                        return true;
                    case R.id.optionAccountSettings:
                        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.maps:
                        Intent intent1 = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(intent1);
                        return true;
                    case R.id.home:
                        Intent intent3 = new Intent(getApplicationContext(), ListEventActivity.class);
                        startActivity(intent3);
                        return true;
                    case R.id.attend:
                        Intent intent5 = new Intent(getApplicationContext(), QrScannerActivity.class);
                        startActivity(intent5);
                        return true;
                    default:
                        return false;
                }

            }
        });

    }


    private void getScheduledEvents() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("scheduled_events");
       events = new ArrayList<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ScheduledEvent scheduledEvent = new ScheduledEvent();
                    Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    try{
                        scheduledEvent.setScheduled_event_id(objectMap.get("scheduled_event_id").toString());
                        scheduledEvent.setDate(objectMap.get("date").toString());
                        scheduledEvent.setImage_description_uri(objectMap.get("image_descriptions_uri").toString());
                        scheduledEvent.setAddress(objectMap.get("address").toString());
                        scheduledEvent.setName(objectMap.get("name").toString());
                        scheduledEvent.setDetails(objectMap.get("details").toString());
                        ArrayList<String> interestedUsers= new ArrayList<>();
                        for(DataSnapshot data: dataSnapshot.child("intersted_users").getChildren())
                        {
                            String user = data.getValue().toString();
                            interestedUsers.add(user);
                        }
                        scheduledEvent.setIntersted_users(interestedUsers);
                        events.add(scheduledEvent);
                    }catch (Error e){
                        Log.e(TAG,"dsadasdasdasdas");
                    }
                }
                setupSched();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupSched(){
            schedulesAdapter = new ScheduledEventAdapter(ShowScheduledEventActivity.this,R.layout.layout_schedules,events);
            mListView.setAdapter(schedulesAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomAppBar.getMenu().getItem(3).setChecked(true);
        checkAuthenticationState();
    }
    private void checkAuthenticationState() {
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(ShowScheduledEventActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }
    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getScheduledEvents();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


}