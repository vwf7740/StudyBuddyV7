package com.jxl.studybuddy;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPasswordActivity extends AppCompatActivity {

    private Button mResetPasswordButton;
    private Button mBackToLoginButton;
    private EditText mEmailForResetPass;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mResetPasswordButton = (Button) findViewById(R.id.button_resetPassword_reset);
        mBackToLoginButton = (Button) findViewById(R.id.button_login_reset);
        mEmailForResetPass = (EditText) findViewById(R.id.editText_email_reset);
        mBackToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = mEmailForResetPass.getText().toString().trim();
        if(!TextUtils.isEmpty(email)){
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "We have sent you instructions to reset your password.", Toast.LENGTH_LONG).show();
                        Intent loginIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                    }else{
                        Toast.makeText(getApplicationContext(), "Failed to send password reset email.", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }else{
            Toast.makeText(getApplicationContext(), "Enter registered email to reset password.", Toast.LENGTH_LONG).show();
        }

    }
}
