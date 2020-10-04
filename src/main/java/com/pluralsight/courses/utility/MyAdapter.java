package com.pluralsight.courses.utility;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.pluralsight.courses.QRCodegenerator;
import com.pluralsight.courses.R;
import com.pluralsight.courses.ListEventActivity;
import com.pluralsight.courses.UsersActivity;
import com.pluralsight.courses.maps.MapsActivity;
import com.pluralsight.courses.users.Event;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends ArrayAdapter<Event> {
    private static final String TAG = "MyAdapter";
    private int mLayoutResource;
    private Context mContext;
    private LayoutInflater mInflater;


    public MyAdapter(@NonNull Context context, int resource, @NonNull List<Event> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public static class ViewHolder{
        TextView name, descritption, address;
        ImageView mEventImage, mTrash,mUsersImage,personAdd;
    }
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.name = (TextView) convertView.findViewById(R.id.creator_name);
            holder.descritption = (TextView) convertView.findViewById(R.id.description);
            holder.personAdd =(ImageView) convertView.findViewById(R.id.generateqrcode);
            holder.mEventImage = (ImageView) convertView.findViewById(R.id.profile_image);
            holder.mTrash = (ImageView) convertView.findViewById(R.id.icon_trash);
            holder.mUsersImage= (ImageView) convertView.findViewById(R.id.users);
            holder.address = convertView.findViewById(R.id.address);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        try{

            holder.name.setText(getItem(position).getEvent_name());
            if(getItem(position).getEvent_description()!= null) {
                holder.descritption.setText(getItem(position).getEvent_description());
            }

            Picasso.with(getContext()).load(getItem(position).getEvent_image_description()).into(holder.mEventImage);
             holder.address.setText( setAdrress(getItem(position).getLocation().getLatitude(),getItem(position).getLocation().getLongitude()));
            if(getItem(position).getEvent_uid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            {
                holder.mTrash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ListEventActivity)mContext).showDeletEventDialog(getItem(position).getEvent_id());
                    }
                });
            }else{
                holder.mTrash.setVisibility(View.INVISIBLE);
            }
        holder.mUsersImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UsersActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("event_id",getItem(position).getEvent_id());
                    bundle.putString("event_uid",getItem(position).getEvent_uid());
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
            holder.personAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                     Intent intent = new Intent(mContext, QRCodegenerator.class);
                     Bundle bundle = new Bundle();
                     bundle.putString("event_id",getItem(position).getEvent_id());
                     intent.putExtras(bundle);
                     mContext.startActivity(intent);
                }
            });

        }catch (NullPointerException e){

            Log.e(TAG, "getView: NullPointerException: ", e.getCause() );
        }

        return convertView;
    }

    private String setAdrress(double latitude, double longitude) {

        try {
            List<Address> addresses = new ArrayList<>();
            Geocoder geocoder = new Geocoder(getContext());
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            if(addresses.size()>0)
            {
                Address adddres = addresses.get(0);
                return adddres.getAddressLine(0);

            }
        }catch (Error | IOException e) {
            Log.e(TAG,"geocoder error"+ e.toString()) ;
           }
        return "No address found";
    }
}


