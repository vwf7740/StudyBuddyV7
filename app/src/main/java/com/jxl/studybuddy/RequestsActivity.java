package com.jxl.studybuddy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsActivity extends BaseActivity {

    private RecyclerView mRequestsRecycler;
    private FirebaseAuth mAuth;
    private DatabaseReference mRequestsDatabase;
    private String mCurrentUser;
    private DatabaseReference mCurrentUserDatabase;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.flContent); //Remember this is the FrameLayout area within your base_main.xml
        getLayoutInflater().inflate(R.layout.activity_requests, contentFrameLayout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserDatabase = FirebaseDatabase.getInstance().getReference().child(mAuth.getCurrentUser().getUid());
        mCurrentUser = mAuth.getCurrentUser().getUid();
        mRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Requests");
        mRequestsRecycler = (RecyclerView)findViewById(R.id.request_list);
        mRequestsRecycler.setHasFixedSize(true);
        mRequestsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Buddies, BuddiesActivity.BuddiesViewHolder> buddiesRecyclerViewAdapter = new FirebaseRecyclerAdapter<Buddies, BuddiesActivity.BuddiesViewHolder>(
                Buddies.class,
                R.layout.user_single_layout,
                BuddiesActivity.BuddiesViewHolder.class,
                mRequestsDatabase
        ){
            @Override
            protected void populateViewHolder(final BuddiesActivity.BuddiesViewHolder buddiesViewHolder, Buddies buddies, int i){
                final String listUserID = getRef(i).getKey();
                final String currentUser = mAuth.getCurrentUser().getUid();
                mUsersDatabase.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                        buddiesViewHolder.setName(userName);
                        buddiesViewHolder.setThumbImage(thumbImage, getApplicationContext());
                        if(listUserID.equals(currentUser)){
                            buddiesViewHolder.mView.setClickable(false);
                            buddiesViewHolder.mView.setEnabled(false);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    buddiesViewHolder.mView.setVisibility(View.GONE);
                                }
                            });
                        }
                        buddiesViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                        Intent profileIntent = new Intent(getApplicationContext(), BuddyProfileActivity.class);
                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        profileIntent.putExtra("user_id", listUserID);
                        startActivity(profileIntent);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        };
        mRequestsRecycler.setAdapter(buddiesRecyclerViewAdapter);
    }

    public static class BuddiesViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public BuddiesViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setDate(String date){
            TextView userCoursesView = (TextView) mView.findViewById(R.id.textView_matchedCourses_users);
            userCoursesView.setText(date);
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.textView_name_users);
            userNameView.setText(name);
        }

        public void setThumbImage(String imageURL, Context context){
            CircleImageView thumbImage = (CircleImageView) mView.findViewById(R.id.imageView_displayPicture_users);
            Glide.with(context)
                    .load(imageURL)
                    .into(thumbImage);
        }
        public void setOnline(String status){
            ImageView online = (ImageView) mView.findViewById(R.id.image_online_icon);
        }
    }

}
