package com.pluralsight.courses.maps;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.pluralsight.courses.EventActivity;
import com.pluralsight.courses.LoginActivity;
import com.pluralsight.courses.QrScannerActivity;
import com.pluralsight.courses.R;
import com.pluralsight.courses.ScheduleEventActivity;
import com.pluralsight.courses.SettingsActivity;
import com.pluralsight.courses.ShowScheduledEventActivity;
import com.pluralsight.courses.ListEventActivity;
import com.pluralsight.courses.users.Event;

import com.pluralsight.courses.users.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {


    private GoogleMap mMap;
    private EditText inputtext;
    private final static String TAG = "MapsActivity";
    BottomNavigationView bottomAppBar;
    Circle circle;
    private String streetAdrres;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        bottomAppBar = findViewById(R.id.bottomBar);

        inputtext =findViewById(R.id.search);

        imageView = findViewById(R.id.ic_magnify);


        mapFragment.getMapAsync(this);
        init();

        bottomAppBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.optionSignOut:
//                        signOut();
                        Intent intent4 = new Intent(getApplicationContext(), ShowScheduledEventActivity.class);
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

    private void init() {
        inputtext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == keyEvent.ACTION_DOWN
                        ||  keyEvent.getAction() == keyEvent.KEYCODE_ENTER){
                    geoLocate();
                }
                return false;
            }
        });
    }

    private void geoLocate() {
        String searchString = inputtext.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> addresses = new ArrayList<>();
        try {
                addresses = geocoder.getFromLocationName(searchString,1);
        }catch (Error | IOException e) {
                Log.e(TAG,"geocoder error"+ e.toString()) ;}
        if(addresses.size()>0)
        {
            moveCamera(new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude()),"Your selected Location");
        }
    }
    public void moveCamera(LatLng latLng,String title){
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (getApplicationContext(), ScheduleEventActivity.class);
                intent.putExtra("address",streetAdrres);
                startActivity(intent);
            }
        });
       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
       mMap.addMarker(new MarkerOptions().position(latLng).title(title).draggable(true).flat(true));

    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomAppBar.getMenu().getItem(1).setChecked(true);
        checkAuthenticationState();
    }

    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("events");
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    final Event event = new Event();
                    Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    event.setEvent_id(objectMap.get("event_id").toString());
                    event.setEvent_name(objectMap.get("event_name").toString());
                    event.setEvent_public((Boolean) objectMap.get("event_public"));
                    event.setEvent_description(objectMap.get("event_description").toString());
                    Location location = dataSnapshot.child("location").getValue(Location.class);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (event.isEvent_public()) {
                        mMap.addMarker(new MarkerOptions().position(latLng)
                                .title(event.getEvent_name())
                                .icon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_baseline_event_24))
                                .snippet(event.getEvent_id()));
                        circle = mMap.addCircle(new CircleOptions()
                                .center(latLng)
                                .radius(100)
                                .strokeWidth(10)
                                .strokeColor(Color.GREEN)
                                .fillColor(Color.argb(128, 255, 0, 0))
                        );
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();

            }
        });
    }
    private BitmapDescriptor bitmapDescriptor (Context context, int vectorResID){
        Drawable drawable = ContextCompat.getDrawable(context,vectorResID);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth()
                ,drawable.getIntrinsicHeight()
                ,Bitmap.Config.ARGB_8888);
        Canvas canvas= new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!marker.isFlat()) {

            Intent intent = new Intent(getApplicationContext(), EventActivity.class);
            intent.putExtra(getString(R.string.intent_event), marker.getSnippet());
            startActivity(intent);
        }
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG,"MArker  drag started");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG,"onmarkerDrag");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG,"onmarkgerDragStop");
        LatLng latLng = marker.getPosition();
        try {
            List<Address> addresses = new ArrayList<>();
            Geocoder geocoder = new Geocoder(MapsActivity.this);
            addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addresses.size()>0)
            {
                Address adddres = addresses.get(0);
                streetAdrres= adddres.getAddressLine(0);
                marker.setTitle(streetAdrres);
            }
        }catch (Error | IOException e) {
            Log.e(TAG,"geocoder error"+ e.toString()) ;}

    }
}