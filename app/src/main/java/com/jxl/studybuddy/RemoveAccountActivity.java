package com.jxl.studybuddy;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RemoveAccountActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mCurrentUser;
    private FirebaseUser mUser;
    private Button mConfirmButton;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.flContent); //Remember this is the FrameLayout area within your base_main.xml
        getLayoutInflater().inflate(R.layout.activity_remove_account, contentFrameLayout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference();
        mDatabaseUsers.keepSynced(true);
        mUser = mAuth.getCurrentUser();
        mCurrentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid().toString());
        mConfirmButton = (Button) findViewById(R.id.button_confirm_removeAccount);
        mCancelButton = (Button) findViewById(R.id.button_cancel_removeAccount);

        //If user hits confirm button, execute confirm() method.
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        //If user hits cancel button, transition back to UserProfileActivity.
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(profileIntent);
            }
        });
    }

    //Opens an alert dialog as further confirmation to remove the user's account, incase they do not wish to do so.
    private void confirm() {
        AlertDialog.Builder alert = new AlertDialog.Builder(RemoveAccountActivity.this);
        alert.setTitle("Please confirm once more");
        alert.setMessage("This will delete your account permanently.").setCancelable(false)

                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Confirm remove account, execute confirmAccountRemoved().
                        confirmAccountRemoved();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Action cancelled, do nothing.
                    }
                });
        alert.show();
    }

    //If user has confirmed twice that they wish to remove their account, remove the account from the database.
    private void confirmAccountRemoved() {
        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();
                }
            }
        });
    }
}
