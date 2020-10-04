package com.pluralsight.courses.utility;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pluralsight.courses.CommentActivity;
import com.pluralsight.courses.EventActivity;
import com.pluralsight.courses.R;
import com.pluralsight.courses.UserDialog;
import com.pluralsight.courses.users.Comment;
import com.pluralsight.courses.users.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentAdapter";
    private int mLayoutResource;
    private Context mContext;
    private LayoutInflater mInflater;

    public CommentListAdapter(@NonNull Context context, int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;

    }
    public static class ViewHolder{
        TextView name, comment;
        ImageView mProfilePicture,mTrash;
    }
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, parent, false);
            holder = new CommentListAdapter.ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.mProfilePicture = (ImageView) convertView.findViewById(R.id.profile_image);
            holder.mTrash = (ImageView) convertView.findViewById(R.id.icon_trash);

        } else {
            holder = (CommentListAdapter.ViewHolder) convertView.getTag();
        }
        try{
            holder.comment.setText(getItem(position).getCommnent());
            holder.comment.setVisibility(View.GONE);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child("users").orderByKey().equalTo(getItem(position).getUser_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                       final User user = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "queri1 :" + user.toString());

                        holder.name.setText(user.getName()+": " + holder.comment.getText());
                        Picasso.with(mContext).load(user.getProfile_picture()).into(holder.mProfilePicture);
                       holder.mProfilePicture.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               UserDialog dialog = new UserDialog();
                               Bundle args = new Bundle();
                               args.putString("user_name",user.getName());
                               args.putString("user_phone",user.getPhone());
                               args.putString("user_profile",user.getProfile_picture());
                               dialog.setArguments(args);
//                Toast.makeText(UsersActivity.this, args.toString(), Toast.LENGTH_LONG).show();
                               dialog.show( ((AppCompatActivity) mContext).getSupportFragmentManager(),"users_dialog");
                           }
                       });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mContext, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            if(((CommentActivity) mContext).getEventUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    || getItem(position).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) )
            {
                holder.mTrash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((CommentActivity)mContext).showDeleteCommentDialog(getItem(position).getComment_id());
                    }
                });
            }else{
                holder.mTrash.setVisibility(View.GONE);
            }

        }catch (NullPointerException e){

            Log.e(TAG, "getView: NullPointerException: ", e.getCause() );
        }
        return convertView;
    }

}
