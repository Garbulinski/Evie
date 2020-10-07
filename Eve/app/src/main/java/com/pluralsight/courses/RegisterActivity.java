package com.pluralsight.courses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.pluralsight.courses.users.User;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText email, password, confirmPassword;
    private Button register;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirmpassword);
        getSupportActionBar().setTitle("Register Activity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        register = (Button) findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmpty(email.getText().toString())
                &&!isEmpty(password.getText().toString())
                &&!isEmpty(confirmPassword.getText().toString())){
                    if(isValidDomain(email.getText().toString())) {
                        if (stringMatch(password.getText().toString(), confirmPassword.getText().toString())) {
                            registerNewEmail(email.getText().toString(),password.getText().toString());

                        } else {
                            Toast.makeText(RegisterActivity.this, "Password don't match", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Email is not valid", Toast.LENGTH_SHORT).show();
                    }
                }else
                {
                    Toast.makeText(RegisterActivity.this,"Please fill out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void registerNewEmail(final String email, String password){
        //showDialog();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG,"onComplete: Completed "+ task.isSuccessful());

                    if(task.isSuccessful())
                    {
                        Log.d(TAG,"onComplete: AuthState"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
                        sendVerificationEmail();
                        User user = new User();
                        user.setName(email.substring(0,email.indexOf("@")));
                        user.setPhone("1");
                        user.setProfile_picture(" ");
                        user.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseAuth.getInstance().signOut();

                                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                FirebaseAuth.getInstance().signOut();

                                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(),"Something Went Wrong",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    else{
                        Toast.makeText(RegisterActivity.this,"Unable to Register", Toast.LENGTH_SHORT).show();
                    }
                   // hideDialog();
                }
        });
    }
    private void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Sent verification Email", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Could not Send verification Email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
    private boolean isValidDomain(String toString) {
       if(Patterns.EMAIL_ADDRESS.matcher(toString).matches())
       {return true;}
       else{return false;}
    }

    private boolean isEmpty (String string){ return string.equals("");};
    private boolean stringMatch(String s1, String s2) {return s1.equals(s2);};
    private void showDialog() {mProgressBar.setVisibility(View.VISIBLE);}
    private void hideDialog(){
        if(mProgressBar.getVisibility()== View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}