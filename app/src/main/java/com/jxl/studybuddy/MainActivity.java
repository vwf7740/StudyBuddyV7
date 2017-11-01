package com.jxl.studybuddy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity {

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView mNameTextView;
    private DatabaseReference mCurrentUser;
    private FirebaseUser mUser;
    private Button mFindBuddiesButton;
    private DatabaseReference mUserRef;
    private static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.flContent); //Remember this is the FrameLayout area within your base_main.xml
        getLayoutInflater().inflate(R.layout.activity_main, contentFrameLayout);

        //setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference();
        mDatabaseUsers.keepSynced(true);
        mNameTextView = (TextView) findViewById(R.id.textView_name_main);
        mUser = mAuth.getCurrentUser();
        mCurrentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid().toString());
        mFindBuddiesButton = (Button) findViewById(R.id.button_findBuddy_main);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserRef.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mCurrentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNameTextView.setText(name);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) { //Forces login screen when app opens if no one is logged in
                    System.out.println("SIGNED OUT");
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                } else {
                    System.out.println("SIGNED IN");
                    mainSequence();
                }

            }

        };

        //When user hits find buddies, transition to SearchActivity
        mFindBuddiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(searchIntent);
            }
        });

    }

    //This method runs when MainActivity starts, even before onCreate().
    @Override
    protected void onStart(){
        super.onStart();
        System.out.println("ON START");
        //Enables detection of authenticated users as soon as activity starts.
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            logOut();
        }else{
            mUserRef.child("online").setValue("true");
        }
    }

    //Remove the listener when it is no longer needed.
    @Override
    protected void onStop(){
        super.onStop();
        System.out.println("ON STOP");
        mAuth.removeAuthStateListener(mAuthListener);
    }

    //Signs out the currently logged in user.
    private void logOut(){
        mAuth.signOut();
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
        finish();
    }

    //Once user has registered, verified, logged in and completed initial setup: main app functions are available.
    private void mainSequence() {
        System.out.println("MAIN SEQUENCE REACHED");

    }

}
