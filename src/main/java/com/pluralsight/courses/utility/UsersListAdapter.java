package com.pluralsight.courses.utility;

import android.content.Context;
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
import com.pluralsight.courses.EventActivity;
import com.pluralsight.courses.R;
import com.pluralsight.courses.UsersActivity;
import com.pluralsight.courses.users.User;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class UsersListAdapter extends ArrayAdapter<User> {
    private static final String TAG = "MyAdapter";
    private int mLayoutResource;
    private Context mContext;
    private LayoutInflater mInflater;
    public UsersListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public static class ViewHolder{
        TextView name, phone;
        ImageView mProfilePicture,mTrash;
    }
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.phone=(TextView)convertView.findViewById(R.id.phone);
            holder.mProfilePicture =(ImageView) convertView.findViewById(R.id.profile_image);
            holder.mTrash = (ImageView) convertView.findViewById(R.id.icon_trash);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
       try {
           holder.name.setText(getItem(position).getName());
           holder.phone.setText(getItem(position).getPhone());
           Picasso.with(getContext()).load(getItem(position).getProfile_picture()).into(holder.mProfilePicture);
           if(((UsersActivity)mContext).getEventUId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) ){

           holder.mTrash.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                            ((UsersActivity)mContext).showDeleteUserDialog(getItem(position).getUser_id());
               }
           });
           }else{
               holder.mTrash.setVisibility(View.GONE);
           }
       }catch (NullPointerException e){
           Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
           Log.e(TAG, "getView: NullPointerException: ", e.getCause() );
       }
       return convertView;
    }
}
