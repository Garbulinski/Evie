package com.pluralsight.courses;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;
import com.pluralsight.courses.users.Event;

public class DeleteImageDialog extends DialogFragment {
    private static final String TAG = "DeleteEventDialog";

    //create a new bundle and set the arguments to avoid a null pointer
    public DeleteImageDialog() {
        super();
        setArguments(new Bundle());
    }

    private String eventId,imageId,userId,evntUid,commentId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: started");
        eventId = getArguments().getString(getString(R.string.event_id_field));
        imageId =getArguments().getString(getString(R.string.image_id_field));
        userId = getArguments().getString("user_id");
        evntUid = getArguments().getString("event_uid");
        commentId = getArguments().getString("commnent_id");
        if (imageId != null) {
            Log.d(TAG, "onCreate: got the image id: " + imageId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delete_event, container, false);


        TextView delete = (TextView) view.findViewById(R.id.confirm_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventId != null && imageId != null && commentId != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                    reference.child("events")
                            .child(eventId)
                            .child("images")
                            .child(imageId)
                            .child("comments")
                            .child(commentId)
                            .removeValue().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failure", Toast.LENGTH_SHORT).show();
                        }
                    });
                            getDialog().dismiss();
                             ((CommentActivity) getActivity()).setupCommnentList();

                } else {
                    if (eventId != null && imageId != null) {
                        Log.d(TAG, "onClick: deleting event: " + eventId);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        reference.child(getString(R.string.dbnone_descriptions))
                                .child(eventId)
                                .child("images")
                                .child(imageId)
                                .removeValue();
                        getDialog().dismiss();
                        ((EventActivity) getActivity()).initImagesList();
                    } else {

                        if (eventId != null && userId != null) {
                            if (evntUid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                                reference.child(getString(R.string.dbnone_descriptions))
                                        .child(eventId)
                                        .child("users")
                                        .child(userId)
                                        .removeValue();
                                getDialog().dismiss();
                                ((UsersActivity) getActivity()).setupUsersList();
                            } else {
                                Toast.makeText(getContext(), "Only the user who created the event can delete other users", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });


        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: cenceling deletion of image");
                getDialog().dismiss();
            }
        });


        return view;
    }
}