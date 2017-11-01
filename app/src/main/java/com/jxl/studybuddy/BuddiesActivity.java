package com.jxl.studybuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.TtsSpan;
import android.view.View;
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

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class BuddiesActivity extends BaseActivity {

    private RecyclerView mBuddyList;
    private FirebaseAuth mAuth;
    private DatabaseReference mBuddiesDatabase;
    private String mCurrentUser;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mCurrentUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.flContent); //Remember this is the FrameLayout area within your base_main.xml
        getLayoutInflater().inflate(R.layout.activity_buddies, contentFrameLayout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid().toString();
        mBuddiesDatabase = FirebaseDatabase.getInstance().getReference().child("Buddies").child(mCurrentUser);
        mBuddiesDatabase.keepSynced(true);
        mBuddyList = (RecyclerView) findViewById(R.id.buddy_list);
        mBuddyList.setHasFixedSize(true);
        mBuddyList.setLayoutManager(new LinearLayoutManager(this));
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mCurrentUserDatabase = FirebaseDatabase.getInstance().getReference().child(mAuth.getCurrentUser().getUid());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Buddies, BuddiesViewHolder> buddiesRecyclerViewAdapter = new FirebaseRecyclerAdapter<Buddies, BuddiesViewHolder>(
            Buddies.class,
            R.layout.user_single_layout,
            BuddiesViewHolder.class,
            mBuddiesDatabase
        ){
            @Override
            protected void populateViewHolder(final BuddiesViewHolder buddiesViewHolder, Buddies buddies, int i){
                buddiesViewHolder.setDate(buddies.getDate());
                final String listUserID = getRef(i).getKey();
                mUsersDatabase.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")){
                            String online = dataSnapshot.child("online").getValue().toString();
                            buddiesViewHolder.setOnline(online);
                        }
                        buddiesViewHolder.setName(userName);
                        buddiesViewHolder.setThumbImage(thumbImage, getApplicationContext());

                        buddiesViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open profile", "Send message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(BuddiesActivity.this);
                                builder.setTitle("Buddy options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which == 0){
                                            Intent profileIntent = new Intent(getApplicationContext(), BuddyProfileActivity.class);
                                            profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            profileIntent.putExtra("user_id", listUserID);
                                            startActivity(profileIntent);
                                        }else if(which == 1){
                                            Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                                            chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            chatIntent.putExtra("user_name", userName);
                                            chatIntent.putExtra("thumb_image", thumbImage);
                                            chatIntent.putExtra("user_id", listUserID);
                                            startActivity(chatIntent);
                                            finish();
                                        }

                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mBuddyList.setAdapter(buddiesRecyclerViewAdapter);
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
            if(status.equals("true")){
                online.setVisibility(View.VISIBLE);
            }else{
                online.setVisibility(View.INVISIBLE);
            }
        }
    }
}
