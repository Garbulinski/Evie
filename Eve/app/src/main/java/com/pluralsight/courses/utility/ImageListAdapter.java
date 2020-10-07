package com.pluralsight.courses.utility;

import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pluralsight.courses.CommentActivity;
import com.pluralsight.courses.EventActivity;
import com.pluralsight.courses.R;
import com.pluralsight.courses.users.Image;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageListAdapter extends ArrayAdapter<Image> {

    private static final String TAG = "ImageListAdapter";

    private int mLayoutResource;
    private Context mContext;
    public ImageListAdapter(@NonNull Context context, int resource, @NonNull List<Image> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
    }
    public static class ViewHolder{
            TextView appreciations ;
        ImageView mProfileImage,trashImage,starImage,addCommnentEdit;
    }
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.appreciations = (TextView) convertView.findViewById(R.id.event_name);
            holder.addCommnentEdit = convertView.findViewById(R.id.comments);
            holder.trashImage = (ImageView) convertView.findViewById(R.id.image_delete);
            holder.starImage = (ImageView) convertView.findViewById(R.id.star);
            holder.mProfileImage = (ImageView) convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
            holder.appreciations.setText("");
        }

        try{

            Picasso.with(getContext()).load(getItem(position).getImage_uri()).into(holder.mProfileImage);

            holder.addCommnentEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Intent intent = new Intent(getContext(), CommentActivity.class);
                        Bundle bundle =new Bundle();
                        bundle.putString("event_id",getItem(position).getImage_eventid());
                        bundle.putString("image_id",getItem(position).getImage_id());
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                }
            });
//
            int a = getItem(position).getApreciations().size();
            holder.appreciations.setText(Integer.toString(a));
            if(getItem(position).getApreciations().contains(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            {
              holder.starImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_baseline_star_24));
            }else{
                holder.starImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child("events")
                                .child(getItem(position).getImage_eventid())
                                .child("images")
                                .child(getItem(position).getImage_id())
                                .child("apreciations")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        holder.appreciations.setText(Integer.toString(getItem(position).getApreciations().size()+1));
                    }
                });
            }
        if(getItem(position).getImage_uid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                || ((EventActivity)mContext).getEventId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) ){
            holder.trashImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EventActivity)mContext).showDeleteImageDialog(getItem(position).getImage_eventid(),getItem(position).getImage_id());
                }
            });
        }else
        {
            holder.trashImage.setVisibility(View.INVISIBLE);
        }

        }catch (NullPointerException e){
            Log.e(TAG, "getView: NullPointerException: ", e.getCause() );

        }

        return convertView;
    }

}
