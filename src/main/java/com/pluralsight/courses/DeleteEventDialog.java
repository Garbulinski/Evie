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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;


public class DeleteEventDialog extends DialogFragment {

    private static final String TAG = "DeleteEventDialog";

    //create a new bundle and set the arguments to avoid a null pointer
    public DeleteEventDialog(){
        super();
        setArguments(new Bundle());
    }

    private String eventId;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: started");
        eventId = getArguments().getString(getString(R.string.event_id_field));
        if(eventId != null){
            Log.d(TAG, "onCreate: got the event id: " + eventId);
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
                if(eventId != null){
                    Log.d(TAG, "onClick: deleting event: " + eventId);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    reference.child(getString(R.string.dbnone_descriptions))
                            .child(eventId)
                            .removeValue();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

                    storageReference.child("events").child(eventId).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (StorageReference prefix : listResult.getPrefixes()) {
                                Toast.makeText(getContext(), prefix.toString(), Toast.LENGTH_SHORT).show();
                                prefix.delete().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Da", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "da", Toast.LENGTH_SHORT).show();
                        }
                    });
                    getDialog().dismiss();
                    ((ListEventActivity)getActivity()).init();
                }
            }
        });

        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: cenceling deletion of event");
                getDialog().dismiss();
            }
        });


        return view;
    }

}

















