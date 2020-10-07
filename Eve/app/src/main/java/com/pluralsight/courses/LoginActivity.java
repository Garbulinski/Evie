package com.pluralsight.courses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText email, password;
    private TextView register, forgotPassword, resendEmail;
    private Button signin;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        getSupportActionBar().setTitle("Login On Evie");
        register = (TextView) findViewById(R.id.register);
        forgotPassword = (TextView) findViewById(R.id.forg);
        resendEmail = (TextView) findViewById(R.id.resend);
        setupFireBaseAuth();
        signin = (Button) findViewById(R.id.login);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEmpty(email.getText().toString())
                   &&!isEmpty((password.getText().toString()))){
                    Log.d(TAG, "onClick: attempting to authentificate ");

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),"Authentification failed",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Fill out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText email2 = new EditText(view.getContext());

                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());

                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter your account email");
                passwordResetDialog.setView(email2);

                passwordResetDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String emailString = email2.getText().toString();
                        if(!emailString.equals("")){
                        firebaseAuth.sendPasswordResetEmail(emailString).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "An reset password email was sent to the specifyed adress", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"This is email is not found in the database",Toast.LENGTH_SHORT).show();
                            }
                        });}else{
                            Toast.makeText(LoginActivity.this, "You must complete the email field", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                passwordResetDialog.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                passwordResetDialog.create().show();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        resendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }
    private boolean isEmpty (String string){ return string.equals("");};
    public void openDialog(){
        ResendVerificationDialog resendEmailDialog = new ResendVerificationDialog();
        resendEmailDialog.show(getSupportFragmentManager(),"example dialog");
    }
    private void setupFireBaseAuth(){
        Log.d(TAG,"setup started");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    if(user.isEmailVerified()){
                        Log.d(TAG,"onAuthStateChanged: signed_id:"+ user.getUid());
                        Toast.makeText(getApplicationContext(),"Authentificated with "+ user.getEmail(),Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, ListEventActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),"Please verify your email before signing in", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
                else{
                    Log.d(TAG,"onAuthStateChanged: signed_oud");
                }
            }
        };
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }
    @Override
    protected void onStop(){
        super.onStop();
        if(mAuthListener!= null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }
}