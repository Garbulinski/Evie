package com.pluralsight.courses;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.squareup.picasso.Picasso;

public class UserDialog extends DialogFragment {
    private String nameb,phoneb,image;
    public UserDialog(){
        super();
        setArguments(new Bundle());
    }
    private TextView name,phone;
    private ImageView profile;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nameb = getArguments().getString("user_name");
        phoneb = getArguments().getString("user_phone");
        image = getArguments().getString("user_profile");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.users_dialog, container, false);
        name = view.findViewById(R.id.name);
        phone =view.findViewById(R.id.phone);
        profile = view.findViewById(R.id.profile_dialog);
        name.setText(nameb);
        phone.setText(phoneb);
        Picasso.with(getContext()).load(image).into(profile);


        return view;
    }
}
