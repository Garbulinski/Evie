package com.pluralsight.courses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pluralsight.courses.maps.MapsActivity;
import com.pluralsight.courses.users.ScheduledEvent;
import com.pluralsight.courses.utility.FilePaths;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class ScheduleEventActivity extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener {
    private static final String TAG = "ScheuleEventActivity";
    private Button buttonPicker,saveEvent;
    private EditText eventNAme, eventDetails;
    private ImageView eventImage;
    private TextView selectDate;
    String scheduledEventKey;

    private DatePickerDialog.OnDateSetListener setListener;
    int PLACE_PICKER_REQUEST = 1;
    private LatLng latLng;
    private String address;
    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;


    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_event);
        getSupportActionBar().setTitle("Schedule a new event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buttonPicker = findViewById(R.id.picker);
        eventDetails = findViewById(R.id.detailsscheduledEvent);
        eventNAme = findViewById(R.id.scheduledEvent);
        eventImage = findViewById(R.id.event_description);
        saveEvent = findViewById(R.id.AddEvent);
        selectDate =findViewById(R.id.tv_date);

        checkAuthenticationState();
        verifyStoragePermissions();
        if(getIntent()!=null)
            address = getIntent().getStringExtra("address");
        Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ScheduleEventActivity.this
                        ,android.R.style.Theme_Holo_Dialog_MinWidth,setListener,year,month,day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    i1 = i1+1;
                    String date = i2+"/"+ i1 +"/" +i;
                    selectDate.setText(date);
            }
        };
        buttonPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
        eventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStoragePermissions){
                    ChangePhotoDialog dialog = new ChangePhotoDialog();
                    dialog.show(getSupportFragmentManager(), getString(R.string.dialog_change_photo));
                }else{
                    verifyStoragePermissions();
                }
            }
        });
        saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScheduledEvent event = new ScheduledEvent();
                if(!eventNAme.getText().toString().equals(""))
                {
                  if(!selectDate.getText().toString().equals("Select Date")){
                      if(address != null){
                          if(mSelectedImageBitmap!= null || mSelectedImageUri !=null){
                              if(!eventDetails.getText().toString().equals("")){
                          event.setDetails(eventDetails.getText().toString());
                          event.setDate(selectDate.getText().toString());
                          event.setName(eventNAme.getText().toString());
                          event.setAddress(address);
                          DatabaseReference reference =FirebaseDatabase.getInstance().getReference();
                          scheduledEventKey = reference.child("scheduled_events").push().getKey();
                          event.setScheduled_event_id(scheduledEventKey);
                          reference.child("scheduled_events").child(scheduledEventKey).setValue(event);
                          if(mSelectedImageUri != null){
                              uploadNewPhoto(mSelectedImageUri);
                          }else if(mSelectedImageBitmap  != null){
                              uploadNewPhoto(mSelectedImageBitmap);
                          }
                              }else{
                                  Toast.makeText(ScheduleEventActivity.this, "You must pick the events details", Toast.LENGTH_SHORT).show();
                              }
                      }else{
                              Toast.makeText(ScheduleEventActivity.this, "You must select a image as a description", Toast.LENGTH_SHORT).show();
                          }

                      }else
                      {
                          Toast.makeText(ScheduleEventActivity.this, "You must select a adress", Toast.LENGTH_SHORT).show();
                      }
                  }else{
                      Toast.makeText(ScheduleEventActivity.this, "You must select a date", Toast.LENGTH_SHORT).show();
                  }
                }else{
                    Toast.makeText(ScheduleEventActivity.this, "You must select a name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
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

            Intent intent = new Intent(ScheduleEventActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
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
            ActivityCompat.requestPermissions(
                    ScheduleEventActivity.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void getImagePath(Uri imagePath) {
        if (!imagePath.toString().equals("")) {
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImageUri: got the image uri: " + mSelectedImageUri);
            Picasso.with(this).load(mSelectedImageUri.toString()).into(eventImage);
        }
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        mSelectedImageUri = null;
        mSelectedImageBitmap = bitmap;
        Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);
        eventImage.setImageBitmap(mSelectedImageBitmap);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
//               latLng = PlacePicker.getPlace(data, this).getLatLng();
                Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    Address add= addresses.get(0);
                    address = add.getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

            Toast.makeText(ScheduleEventActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(ScheduleEventActivity.this.getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText( ScheduleEventActivity.this, "That image is too large.", Toast.LENGTH_SHORT).show();
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
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(filePaths.FIREBASE_SCHEDULEDEVENTS_IMAGE_STORAGE + "/" + scheduledEventKey
                        + "/description_image"); //just replace the old image with the new one

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


                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri firebaseURL = task.getResult();
                        Toast.makeText(ScheduleEventActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL.toString());
                        FirebaseDatabase.getInstance().getReference()
                                .child("scheduled_events")
                                .child(scheduledEventKey)
                                .child("image_descriptions_uri")
                                .setValue(firebaseURL.toString());

                    } else {
                        Toast.makeText(getApplicationContext(), "Eroare", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }
    }
}