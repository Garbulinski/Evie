package com.pluralsight.courses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pluralsight.courses.users.Event;
import com.pluralsight.courses.utility.FilePaths;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddEventActivty extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener, LocationListener {

    private static final String TAG = "AddEventActivity";
    private ImageView imageView;
    private EditText eventName, eventDescrioption;
    private CheckBox publiccheckBox;
    private Button addEventButton;
    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar;
    private TextView textView;
    private Uri mSelectedUri;
    private Bitmap mSelectedBitmap;
    private byte[] mBytes;
    private boolean mStoragePermissions;
    private static final double MB = 1000000.0;
    private static final double MB_THRESHHOLD = 5.0;
    private static final int REQUEST_CODE = 1234;
   private Event event;
    com.pluralsight.courses.users.Location location1;


    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        getSupportActionBar().setTitle("Start a new event");
        imageView = findViewById(R.id.addPicture);
        eventName = findViewById(R.id.eventName);
        eventDescrioption = findViewById(R.id.description);
        publiccheckBox = findViewById(R.id.checkBox);
        addEventButton = findViewById(R.id.buttonAddLocation);
        mProgressBar = findViewById(R.id.progressBar);

        event = new Event();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStoragePermissions) {
                    ChangePhotoDialog dialog = new ChangePhotoDialog();
                    dialog.show(getSupportFragmentManager(), getString(R.string.dialog_change_photo));
                } else {
                    verifyStoragePermissions();
                }

            }
        });

            addEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSelectedUri != null || mSelectedBitmap != null) {

                        if (eventName.getText().toString().equals("")) {
                            Toast.makeText(AddEventActivty.this, "Name field must be completed", Toast.LENGTH_SHORT).show();
                        } else {
                            location1 = new com.pluralsight.courses.users.Location();
                            event.setEvent_name(eventName.getText().toString());
                            event.setEvent_description(eventDescrioption.getText().toString());
                            event.setEvent_uid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            event.setEvent_public(publiccheckBox.isChecked());
                            if (ActivityCompat.checkSelfPermission(AddEventActivty.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Location> task) {
                                        Location location = task.getResult();
                                        if (location != null) {
                                            try {
                                                Geocoder geocoder = new Geocoder(AddEventActivty.this, Locale.getDefault());
                                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                                        location.getLongitude(),
                                                        1);

                                                location1.setLatitude(addresses.get(0).getLatitude());
                                                location1.setLongitude(addresses.get(0).getLongitude());
                                                event.setLocation(location1);
//                                                mDatabase = FirebaseDatabase.getInstance().getReference();
//                                                eventKey = mDatabase.child("events").push().getKey();
//                                                event.setEvent_id(eventKey);
//
//                                                mDatabase.child("events").child(eventKey).setValue(event);
//                                                mDatabase.child("events")
//                                                        .child(eventKey)
//                                                        .child("location")
//                                                        .setValue(location1);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            } else {
                                ActivityCompat.requestPermissions(AddEventActivty.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                            }
//
                            if (mSelectedUri != null) {
                                uploadNewPhoto(mSelectedUri);
//
                            } else if (mSelectedBitmap != null) {
                                uploadNewPhoto(mSelectedBitmap);
                            }
                            Intent intent = new Intent(getApplicationContext(),ListEventActivity.class);
                            startActivity(intent);
                        }

                    }else{
                        Toast.makeText(AddEventActivty.this, "You must select a image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    }
    @Override
    public void getImagePath(Uri imagePath) {
        if(!imagePath.toString().equals(""))
        {
            mSelectedBitmap = null;
            mSelectedUri = imagePath;
            Log.d(TAG, "image uri = "+ mSelectedUri);
            Picasso.with(this).load(mSelectedUri.toString()).into(imageView);
        }
    }


    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap != null){
            mSelectedUri = null;
            mSelectedBitmap = bitmap;
            Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedBitmap);
            imageView.setImageBitmap(bitmap);
        }
    }
    public void uploadNewPhoto(Uri imageUri){
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }
    public void uploadNewPhoto(Bitmap imageBitmap){

        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");

        BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
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
            Toast.makeText(getApplicationContext(), "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(getApplicationContext(), "That image is too large.", Toast.LENGTH_SHORT).show();
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
    private void executeUploadTask(){

        FilePaths filePaths = new FilePaths();
//specify where the photo will be stored

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final String eventKey = reference.child("events").push().getKey();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(filePaths.FIREBASE_EVENTS_IMAGE_STORAGE  + "/" + eventKey
                        + "/description_image");

        if(mBytes.length/MB < MB_THRESHHOLD) {

            // Create file metadata including the content type

            //if the image size is valid then we can submit to database

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes);
            //uploadTask = storageReference.putBytes(mBytes); //without metadata


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
                        Toast.makeText(getApplicationContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL.toString());
                        event.setEvent_id(eventKey);
                        event.setEvent_image_description(firebaseURL.toString());
                        FirebaseDatabase.getInstance().getReference()
                                        .child("events")
                                        .child(eventKey)
                                        .setValue(event);
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
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    private void checkAuthenticationState() {
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(AddEventActivty.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
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
            ActivityCompat.requestPermissions(AddEventActivty.this, permissions,
                    REQUEST_CODE
            );
        }
    }
}