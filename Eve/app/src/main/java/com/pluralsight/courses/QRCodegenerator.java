package com.pluralsight.courses;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRCodegenerator extends AppCompatActivity {
    private static final String TAG ="QRCodegenerator";
    private String eventID;
    private ImageView qrcodeImage;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_generatorgenerator);
        Bundle bundle = getIntent().getExtras();
        eventID= bundle.getString("event_id");
        qrcodeImage = (ImageView) findViewById(R.id.qrcodeImage);
        getSupportActionBar().setTitle("Your event Code");

        QRGEncoder qrgEncoder = new QRGEncoder(eventID,null, QRGContents.Type.TEXT,500);
        try {
                bitmap = qrgEncoder.getBitmap();
            qrcodeImage.setImageBitmap(bitmap);
        }catch (Error e)
        {
            Log.e(TAG,e.toString());
        }
    }
}