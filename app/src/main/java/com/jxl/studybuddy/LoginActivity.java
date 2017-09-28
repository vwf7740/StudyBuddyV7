package com.jxl.studybuddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mLoginEmail;
    private EditText mLoginPassword;
    private Button mLoginButton;
    private Button mSignUpButton;
    private Button mForgotPasswordButton;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mLoginEmail = (EditText) findViewById(R.id.text_loginEmail) ;
        mLoginPassword = (EditText) findViewById(R.id.text_loginPassword);
        mLoginButton = (Button) findViewById(R.id.button_login);
        mSignUpButton = (Button) findViewById(R.id.button_signUp);
        mForgotPasswordButton = (Button) findViewById((R.id.button_forgotPassword_login));
        //If user hits login button, execute checkLogin().
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });
        //If user hits sign up button, transition to RegisterActivity.
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPasswordIntent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(resetPasswordIntent);
            }
        });
    }

    //Authenticates user with entered email and password.
    private void checkLogin() {
        String email = mLoginEmail.getText().toString().trim();
        String password = mLoginPassword.getText().toString().trim();
        //email and password fields must not be empty to proceed.
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            mProgress.setMessage("Authenticating user...");
            mProgress.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mProgress.dismiss();
                        //If authentication is successful, execute checkUserExists().
                        checkUserExists();
                    }else{
                        mProgress.dismiss();
                        Toast.makeText(LoginActivity.this, "Invalid login.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    //Check if authenticated user already exists on DB.
    private void checkUserExists() {
        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Check if user exists.
                if(dataSnapshot.hasChild(user_id)){
                    //Check if user is email verified.
                    if(!mAuth.getCurrentUser().isEmailVerified()){
                        //Send user back to login if not verified.
                        Toast.makeText(getApplicationContext(), "Email not verified.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        //If user is verified, check if user has set up account.
                    }else if(dataSnapshot.child(user_id).child("image").getValue().toString().equals("default")){
                        //If user hasn't set up their account, transition to SetupActivity.
                        Toast.makeText(getApplicationContext(), "Please setup your account", Toast.LENGTH_LONG).show();
                        Intent setupIntent = new Intent(LoginActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }else{
                        //If the user exists and is email verified, transition to MainActivity.
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
