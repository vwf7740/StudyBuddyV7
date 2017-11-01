package com.jxl.studybuddy;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class BuddyProfileActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private FirebaseUser mCurrentUser;
    private ImageView mBuddyDisplayPicture;
    private TextView mName;
    private Button mCourse1Button;
    private Button mCourse2Button;
    private Button mCourse3Button;
    private Button mCourse4Button;
    private ProgressDialog mProgress;
    private String mBuddyStatus;
    private Button mRequestButton;
    private DatabaseReference mRequestDatabase;
    private DatabaseReference mBuddiesDatabase;
    private DatabaseReference mNotificationsDatabase;
    private DatabaseReference mRootRef;
    private DatabaseReference mCurrentUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.flContent); //Remember this is the FrameLayout area within your base_main.xml
        getLayoutInflater().inflate(R.layout.activity_buddy_profile, contentFrameLayout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final String user_id = getIntent().getStringExtra("user_id");
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mDatabaseUsers.keepSynced(true);
        mCurrentUser = mAuth.getCurrentUser();
        mCurrentUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mBuddyDisplayPicture = (ImageView) findViewById(R.id.imageView_displayPicture_buddyProfile);
        mCourse1Button = (Button) findViewById(R.id.button_courseCode1_buddyProfile);
        mCourse2Button = (Button) findViewById(R.id.button_courseCode2_buddyProfile);
        mCourse3Button = (Button) findViewById(R.id.button_courseCode3_buddyProfile);
        mCourse4Button = (Button) findViewById(R.id.button_courseCode4_buddyProfile);
        mName = (TextView) findViewById(R.id.textView_name_buddyProfile);
        mRequestButton = (Button) findViewById(R.id.button_addOrRemove_buddyProfile);
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Fetching user data...");
        mProgress.setCanceledOnTouchOutside(false);
        mBuddyStatus = "not_friends";
        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Requests");
        mBuddiesDatabase = FirebaseDatabase.getInstance().getReference().child("Buddies");
        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        /*
        TO DO:
        -Load buddy info.
        -Load their display picture.
        -Load their courses. Set course button invisible if doesn't contain course.
        -If the buddy is on the user's buddy list, the bottom button will be 'Remove buddy'.
        if the buddy is not on the user's buddy list, the button will be 'Add buddy'.
        -Clicking the 'send message' button will take the user to a chat page with that buddy.
        This button will be invisible if the buddy is not on the user's buddy list.
         */

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String name = dataSnapshot.child("name").getValue().toString();
                final String courses = dataSnapshot.child("courses").getValue().toString();
                final String imageUrl = dataSnapshot.child("image").getValue().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.show();
                        Glide.with(getApplicationContext())
                                .load(imageUrl)
                                .into(mBuddyDisplayPicture);
                        mName.setText(name);
                        StringTokenizer tk = new StringTokenizer(courses, "#");
                        int count = 1;
                        while(tk.hasMoreTokens()){
                            if(count == 1){
                                mCourse1Button.setText(tk.nextToken());
                                mCourse1Button.setVisibility(View.VISIBLE);
                                mCourse1Button.setClickable(false);
                            }else if(count == 2){
                                mCourse2Button.setText(tk.nextToken());
                                mCourse2Button.setVisibility(View.VISIBLE);
                                mCourse2Button.setClickable(false);
                            }else if(count == 3){
                                mCourse3Button.setText(tk.nextToken());
                                mCourse3Button.setVisibility(View.VISIBLE);
                                mCourse3Button.setClickable(false);
                            }else if(count == 4){
                                mCourse4Button.setText(tk.nextToken());
                                mCourse4Button.setVisibility(View.VISIBLE);
                                mCourse4Button.setClickable(false);
                            }
                            count++;
                        }

                    }

                });
                mRequestDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                mBuddyStatus = "received";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRequestButton.setText("Accept request");
                                    }
                                });
                            } else if (req_type.equals("sent")) {
                                mBuddyStatus = "sent";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRequestButton.setText("Cancel buddy request");
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mBuddiesDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            mBuddyStatus = "friends";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRequestButton.setText("Remove buddy");
                                }
                            });
                        }else{
                            mBuddyStatus = "not_friends";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRequestButton.setText("Request buddy");
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestButton.setEnabled(false);
                //-------------------------SEND REQUEST-------------------------------
                if(mRequestButton.getText().equals("Request buddy")){//initiating request.
                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationID = newNotificationRef.getKey();
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");
                    Map requestMap = new HashMap<>();
                    requestMap.put("Requests/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Requests/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationID, notificationData);
                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BuddyProfileActivity.this, "Error sending request.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                mBuddyStatus = "sent";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRequestButton.setText("Cancel buddy request");
                                    }
                                });
                            }
                        }
                    });
                    //----------------------CANCEL REQUEST----------------------------------
                }else if(mRequestButton.getText().equals("Cancel buddy request")){//cancelling request.
                    Map cancelMap = new HashMap();
                    cancelMap.put("Requests/" + mCurrentUser.getUid() + "/" + user_id, null);
                    cancelMap.put("Requests/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(cancelMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BuddyProfileActivity.this, "Error cancelling request.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                mBuddyStatus = "not_friends";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRequestButton.setText("Request buddy");
                                    }
                                });
                            }
                        }
                    });
                //--------------------REMOVE BUDDY-----------------------------------
                }else if(mRequestButton.getText().equals("Remove buddy")){
                    Map removeBuddyMap = new HashMap();
                    removeBuddyMap.put("Buddies/" + mCurrentUser.getUid() + "/" + user_id, null);
                    removeBuddyMap.put("Buddies/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(removeBuddyMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BuddyProfileActivity.this, "Error removing buddy.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                mBuddyStatus = "not_friends";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRequestButton.setText("Request buddy");
                                    }
                                });
                            }
                        }
                    });
                }
                //--------------------REQUEST RECEIVED---------------------------
                if(mBuddyStatus.equals("received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map buddiesMap = new HashMap();
                    buddiesMap.put("Buddies/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    buddiesMap.put("Buddies/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);
                    buddiesMap.put("Requests/" + mCurrentUser.getUid() + "/" + user_id, null);
                    buddiesMap.put("Requests/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(buddiesMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BuddyProfileActivity.this, "Error adding buddy.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                mBuddyStatus = "friends";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRequestButton.setText("Remove buddy");
                                    }
                                });
                            }
                        }
                    });
                }
                mRequestButton.setEnabled(true);
            }
        });
    }
}
