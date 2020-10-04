package com.pluralsight.courses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pluralsight.courses.users.Comment;
import com.pluralsight.courses.users.Event;
import com.pluralsight.courses.utility.CommentListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    private static final String TAG = "UsersActivity";

    private FirebaseAuth.AuthStateListener mAuthListener;
    private ListView listView;
    private CommentListAdapter adapter;
    private ArrayList<Comment> comments;
    private DatabaseReference reference;
    private String eventId, imageID;
    private TextView actionButton;
    private EditText comment;
    String eventUID ;
    private ImageView back;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commnent);
        Bundle bundle = getIntent().getExtras();
        eventId = bundle.getString("event_id");
        eventUID = new String();
        imageID = bundle.getString("image_id");

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.appbar);
        View view = getSupportActionBar().getCustomView();
        back = view.findViewById(R.id.back123321);
        listView = (ListView)findViewById(R.id.commentsListView);
        actionButton=  findViewById(R.id.addWritenCommnent);
        comment =(EditText) findViewById(R.id.editCommentText);
//        comment.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);
        getEventUid();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(getApplicationContext(),EventActivity.class);
               intent.putExtra("intent_event",eventId);
               startActivity(intent);
            }
        });
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Comment commentAdded = new Comment();
                commentAdded.setCommnent(comment.getText().toString());
                commentAdded.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                reference = FirebaseDatabase.getInstance().getReference();
                String commentKey= reference.child("events")
                        .child(eventId)
                        .child("images")
                        .child(imageID)
                        .child("comments")
                        .push().getKey();
                commentAdded.setComment_id(commentKey);
                reference.child("events")
                        .child(eventId)
                        .child("images")
                        .child(imageID)
                        .child("comments")
                        .child(commentKey)
                        .setValue(commentAdded);
                comment.setText("");
            }
        });
        enableUserListener();

    }


    private void enableUserListener(){
        reference = FirebaseDatabase.getInstance().getReference().child("events").child(eventId).child("images").child(imageID).child("comments");
        reference.addValueEventListener(mValueEventListener);
    }
    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getCommnents();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
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
            Intent intent = new Intent(CommentActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }

    public void setupCommnentList(){
       // Toast.makeText(this, comments.toString(), Toast.LENGTH_SHORT).show();
            adapter = new CommentListAdapter(CommentActivity.this,R.layout.layout_commnent_list,comments);
        listView.setAdapter(adapter);
    }
    private void getCommnents() {
        comments = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("events")
                .child(eventId)
                .child("images")
                .child(imageID)
                .child("comments");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot singlesnap: snapshot.getChildren()){
                    Comment commnent = new Comment();
                    commnent= singlesnap.getValue(Comment.class);
                    comments.add(commnent);
                }

                setupCommnentList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public String getEventUid()
    {
        reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child("events")
                .child(eventId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data: snapshot.getChildren() ){

                    Map<String, Object> objectMap = (HashMap<String, Object>) snapshot.getValue();
                    eventUID =objectMap.get("event_uid").toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
            }
        });
        return eventUID;
    }
    public void showDeleteCommentDialog(String commnentId){
        DeleteImageDialog dialog = new DeleteImageDialog();
        Bundle args = new Bundle();
        args.putString(getString(R.string.event_id_field), eventId);
        args.putString(getString(R.string.image_id_field),imageID);
        args.putString("commnent_id",commnentId);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_delete_image));
    }


}