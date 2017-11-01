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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.Console;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mRegisterButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private ProgressDialog mProgress;
    private Button mBackToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mProgress = new ProgressDialog(this);
        mNameField = (EditText) findViewById(R.id.text_name);
        mEmailField = (EditText) findViewById(R.id.text_email);
        mPasswordField = (EditText) findViewById(R.id.text_password);
        mRegisterButton = (Button) findViewById(R.id.button_register);
        mBackToLoginButton = (Button) findViewById(R.id.button_backToLogin_register);

        mBackToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        //When user hits register button, execute startRegister() but only if password is longer than 6 chars.
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mPasswordField.getText().toString().trim();
                if(password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password must be 6 characters or longer.", Toast.LENGTH_LONG).show();
                }else {
                    startRegister();
                }
            }
        });
    }

    //Creates a new user on the DB with their name, email and password, and default values for
    //thumb_image, courses and sets false for init_setup to indicate they need to complete initial setup
    //before they can use the app.
    private void startRegister() {
        final String name = mNameField.getText().toString().trim();
        final String email = mEmailField.getText().toString().trim();
        final String password = mPasswordField.getText().toString().trim();
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            mProgress.setMessage("Signing up...");
            mProgress.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = mDatabaseUsers.child(user_id);
                        String tokenID = FirebaseInstanceId.getInstance().getToken();
                        HashMap<String, String> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("image", "default");
                        userMap.put("thumb_image", "default");
                        userMap.put("image", "default");
                        userMap.put("courses", "default");
                        userMap.put("token_id", tokenID);
                        current_user_db.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mProgress.dismiss();
                                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            //Signs in the newly created user, if it's successful, executes verifyEmail().
                                            verifyEmail();

                                        }
                                    });
                                }else{
                                    mProgress.dismiss();
                                    Toast.makeText(getApplicationContext(), "Failed to register new user.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });
        }else{
            //Displays message if user has not input all required fields.
            Toast.makeText(getApplicationContext(), "All fields are required.", Toast.LENGTH_LONG).show();
        }
    }

    //Sends a verification email to user's supplied email.
    private void verifyEmail() {
        final FirebaseUser user = mAuth.getInstance().getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //If email is sent successfully, transition to VerifyActivity
                    Toast.makeText(getApplicationContext(), "Verification email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                    Intent verifyIntent = new Intent(RegisterActivity.this, VerifyActivity.class);
                    verifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(verifyIntent);
                }else{
                    Toast.makeText(getApplicationContext(), "Failed to send verification email to " + user.getEmail(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
