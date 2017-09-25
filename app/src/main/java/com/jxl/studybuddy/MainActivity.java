package com.jxl.studybuddy;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference();
        mDatabaseUsers.keepSynced(true);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){ //Forces login screen when app opens if no one is logged in
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }else{
                    checkUserExists();
                }

            }

        };

    }

    @Override
    //Populates MainActivity options menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    //If user hits logout button in MainActivity options menu, user is signed out and app returns to LoginActivity.
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_logout){
            logOut();
        }
        return super.onOptionsItemSelected(item);
    }

    //This method runs when MainActivity starts, even before onCreate()
    protected void onStart(){
        super.onStart();
        //Enables detection of authenticated users as soon as activity starts.
        mAuth.addAuthStateListener(mAuthListener);
    }

    //Signs out the currently logged in user
    private void logOut(){
        mAuth.signOut();
    }

    //If someone is logged in, check if the user exists on the DB.
    private void checkUserExists() {
        mAuth.getCurrentUser().reload();
        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user_id)){
                    final FirebaseUser user = mAuth.getCurrentUser();
                    if(!mAuth.getCurrentUser().isEmailVerified()){
                        //Checks if the user is email verified. If not, signs out and changes to LoginActivity.
                        Toast.makeText(getApplicationContext(), "Email not verified.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }else if(dataSnapshot.child("init").getValue().toString().equals("incomplete")){
                        //If the user has not completed initial setup, go to SetupActivity.
                        Toast.makeText(getApplicationContext(), "Please setup your account", Toast.LENGTH_LONG).show();
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //User can't leave login page without logging in or registering
                        startActivity(setupIntent);
                    }else{
                        //If a user is logged in, is email verified, and has completed initial setup: continue to main app functions.
                        mainSequence();
                    }
                }else{
                    //No user found on DB, returns to LoginActivity.
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Once user has registered, verified, logged in and completed initial setup: main app functions are available.
    private void mainSequence() {

    }
}
