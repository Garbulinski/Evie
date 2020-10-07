package com.pluralsight.courses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pluralsight.courses.users.Event;
import com.pluralsight.courses.users.User;
import com.pluralsight.courses.utility.UsersListAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.media.CamcorderProfile.get;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG = "UsersActivity";

    private FirebaseAuth.AuthStateListener mAuthListener;


    DatabaseReference reference;
    private UsersListAdapter adapter;
    private ArrayList<User> users;
    private ListView listView;
    private String eventID,eventUId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        getSupportActionBar().setTitle("Attended Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView)findViewById(R.id.usersListView);

        Bundle bundle = getIntent().getExtras();
        eventID = bundle.getString("event_id");
        eventUId = bundle.getString("event_uid");
        enableUserListener();


    }

    public String getEventUId() {
        return eventUId;
    }

    private void getUser() {
         reference = FirebaseDatabase.getInstance().getReference();
        users = new ArrayList<>();
        Query query =  reference.child(getString(R.string.dbnone_descriptions))
                .child(eventID)
                .child("users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    final String userID = dataSnapshot.getValue(String.class);
                    Query query1 = reference.child(getString(R.string.dbnode_users));

                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {

                            for(DataSnapshot data : snapshot2.getChildren()) {
                                User user = new User();
                                user.setUser_id(data.getValue(User.class).getUser_id());
                                user.setName(data.getValue(User.class).getName());
                                user.setProfile_picture(data.getValue(User.class).getProfile_picture());
                                user.setPhone(data.getValue(User.class).getPhone());
                                if(user.getUser_id().equals(userID)) {
                                    users.add(user);
                                }
                            }
                            if(users.size() > 0)
                            { setupUsersList();}
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UsersActivity.this, "query1: "+error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                Log.e(TAG,error.toString());
            }
        });

    }

    public void setupUsersList() {
        adapter = new UsersListAdapter(UsersActivity.this,R.layout.layout_list_users,users);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                UserDialog dialog = new UserDialog();
                Bundle args = new Bundle();
                args.putString("user_name",users.get(i).getName());
                args.putString("user_phone",users.get(i).getPhone());
                args.putString("user_profile",users.get(i).getProfile_picture());
                dialog.setArguments(args);
//                Toast.makeText(UsersActivity.this, args.toString(), Toast.LENGTH_LONG).show();
                dialog.show(getSupportFragmentManager(), "users_dialog");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkAuthenticationState();
    }



    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(UsersActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }

    private void enableUserListener(){

//        Toast.makeText(this, eventId, Toast.LENGTH_SHORT).show();
        reference = FirebaseDatabase.getInstance().getReference().child("events").child(eventID).child("users");
        reference.addValueEventListener(mValueEventListener);

    }
    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getUser();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void showDeleteUserDialog(String userId){
        DeleteImageDialog dialog = new DeleteImageDialog();
        Bundle args = new Bundle();
        args.putString(getString(R.string.event_id_field), eventID);
        args.putString("user_id",userId);
        args.putString("event_uid",eventUId);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_delete_image));
    }

}