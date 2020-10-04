package com.pluralsight.courses;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pluralsight.courses.maps.MapsActivity;
import com.pluralsight.courses.users.Event;
import com.pluralsight.courses.users.Image;
import com.pluralsight.courses.utility.FilePaths;
import com.pluralsight.courses.utility.ImageListAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventActivity extends AppCompatActivity {
    private static final String TAG = "EventActivity";

    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference reference;
     int i=0;

    private ListView mListView;
    private Event event;
    private List<Image> mImageList;
    private ImageListAdapter mAdapter;
    private String eventId;
    int PICK_IMAGE_MULTIPLE = 1;
    Uri imageEncoded;
    List<Uri> imagesEncodedList;
    private static final int REQUEST_CODE = 1234;
    private boolean mStoragePermissions;

    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;
    private byte[] mBytes;
    BottomNavigationView bottomAppBar;
    FloatingActionButton buttonAdd;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        getSupportActionBar().setTitle("Display Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buttonAdd = findViewById(R.id.addimageevent);
        imagesEncodedList = new ArrayList<Uri>();
        mListView = (ListView) findViewById(R.id.listView);
        mImageList = new ArrayList<>();

        verifyStoragePermissions();
        setupFirebaseAuth();

        getEvent();

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStoragePermissions){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent,"Select Pictures"), PICK_IMAGE_MULTIPLE);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                if(data.getData()!=null){

                    Uri mImageUri=data.getData();
                    imageEncoded  = mImageUri;

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            imagesEncodedList.add(uri);
                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void verifyStoragePermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    EventActivity.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }
    public void uploadNewPhoto(Uri imageUri){
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(EventActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(EventActivity.this.getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(EventActivity.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
                Log.d(TAG, "doInBackground: megabytes: (" + (11-i) + "0%) "  + bytes.length/MB + " MB");
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);

            mBytes = bytes;
            //execute the upload

            executeUploadTask();
        }
    }
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeUploadTask(){
        FilePaths filePaths = new FilePaths();
//specify where the photo will be stored
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final String imageKey = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnone_descriptions))
                .child(event.getEvent_id())
                .child("images")
                .push().getKey();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(filePaths.FIREBASE_EVENTS_IMAGE_STORAGE + "/" + eventId   +"/event_images/"
                        +imageKey);

        if(mBytes.length/MB < MB_THRESHHOLD) {


            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri firebaseURL = task.getResult();
                        Toast.makeText(EventActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL.toString());

                        Image image = new Image();
                        image.setImage_uri(firebaseURL.toString());
                        image.setImage_id(imageKey);
                        image.setImage_eventid(event.getEvent_id());
                        image.setImage_uid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.dbnone_descriptions))
                                .child(event.getEvent_id())
                                .child("images")
                                .child(imageKey).setValue(image);
                    } else {
                        Toast.makeText(getApplicationContext(), "Eroare", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            ;
        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }

    }
    public String getEventId(){
        return event.getEvent_uid();
    }

    private void getEvent() {
        Log.d(TAG, "getEvent: getting selected event details");

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.intent_event))) {
            eventId = intent.getExtras().getString(getString(R.string.intent_event));

            reference = FirebaseDatabase.getInstance().getReference();

            if (eventId != null) {
                Query query1 = reference.child(getString(R.string.dbnone_descriptions))
                        .orderByKey()
                        .equalTo(eventId);
                if (query1 != null) {
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Asdasd");

                            event = new Event();
                            for(DataSnapshot singleSnapshot:  dataSnapshot.getChildren()) {

                                Log.d(TAG, "onDataChange: found chatroom: "
                                        + singleSnapshot.getValue());
                                Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                event.setEvent_id(objectMap.get("event_id").toString());
                                event.setEvent_name(objectMap.get("event_name").toString());
                                event.setEvent_uid(objectMap.get("event_uid").toString());
                            }

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), "couldont make query", Toast.LENGTH_SHORT).show();
                        }

                    });
                }

            enableEventListener();
            }
        }
    }
    private void getEventImages(){
        mImageList = new ArrayList<>();
        if(mImageList.size() > 0){
            mImageList.clear();
            mAdapter.clear();
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnone_descriptions))
                .child(eventId)
                .child("images");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {

                    DataSnapshot snapshot = singleSnapshot;
                    Log.d(TAG, "onDataChange: found event images: "
                            + singleSnapshot.getValue());
                    try {
                        Image image = new Image();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        ArrayList<String> app = new ArrayList<>();
                        for(DataSnapshot snap : singleSnapshot.child("apreciations").getChildren())
                        {
                                String string= new String();
                                string = snap.getValue(String.class);
                                app.add(string);
                        }
                        image.setApreciations(app);
                        image.setImage_uri(objectMap.get("image_uri").toString());
                        image.setImage_id(objectMap.get("image_id").toString());
                        image.setImage_eventid(objectMap.get("image_eventid").toString());
                        image.setImage_uid(objectMap.get("image_uid").toString());

                        mImageList.add(image);
                    } catch (NullPointerException e) {
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }
                initImagesList();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initImagesList(){

        mAdapter = new ImageListAdapter(EventActivity.this, R.layout.layout_imagelist, mImageList);
        mListView.setAdapter(mAdapter);
        final int[] inte = {0};
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                inte[0]++;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(inte[0] ==1){

                        }else if(inte[0] == 2){

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child("events")
                                    .child(mImageList.get(i).getImage_eventid())
                                    .child("images")
                                    .child(mImageList.get(i).getImage_id())
                                    .child("apreciations")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        }
                        inte[0]=0;
                    }
                },500);
            }
        });
//        mListView.setSelection(mAdapter.getCount() - 1); //scroll to the bottom of the list
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();

        if(imageEncoded != null)
        {
            uploadNewPhoto(imageEncoded);
        }
        if(imagesEncodedList!=null)
        {
            for(Uri encoded:imagesEncodedList){
                uploadNewPhoto(encoded);
            }
        }
        else{
            Toast.makeText(EventActivity.this, "Images list is null", Toast.LENGTH_SHORT).show();
        }
        imagesEncodedList.clear();
        imageEncoded = null;
    }
    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(EventActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());


                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(EventActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EventActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(mValueEventListener);
    }
    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getEventImages();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private void enableEventListener(){
        reference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnone_descriptions))
                .child(eventId)
                .child(getString(R.string.field_event_images));
        reference.addValueEventListener(mValueEventListener);
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
//
    }

    public void showDeleteImageDialog(String eventId,String imageId){
        DeleteImageDialog dialog = new DeleteImageDialog();
        Bundle args = new Bundle();
        args.putString(getString(R.string.event_id_field), eventId);
        args.putString(getString(R.string.image_id_field),imageId);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_delete_image));
    }
}
