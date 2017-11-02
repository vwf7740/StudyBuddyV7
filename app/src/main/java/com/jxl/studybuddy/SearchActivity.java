package com.jxl.studybuddy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends BaseActivity {

    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mCurrentUser;
    DatabaseReference mMatchedDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ProgressDialog mProgress;
    private RecyclerView mUserList;
    private String mCurrentUserCourses;
    private HashSet<String> mCurrentUserCoursesSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.flContent); //Remember this is the FrameLayout area within your base_main.xml
        getLayoutInflater().inflate(R.layout.activity_search, contentFrameLayout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mCurrentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid().toString());
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mMatchedDatabase = FirebaseDatabase.getInstance().getReference().child("matches").child(mAuth.getCurrentUser().getUid());
        mProgress = new ProgressDialog(this);
        mUserList = (RecyclerView) findViewById(R.id.user_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
    }

    public boolean checkCourses(String courses){
        final HashSet<String> childCoursesSet = new HashSet<String>();
        StringTokenizer tk = new StringTokenizer(courses, "#");
        while(tk.hasMoreTokens()){
            childCoursesSet.add(tk.nextToken());
        }
        System.out.println("CHILD COURSES: " + childCoursesSet.toString());
        if((!Collections.disjoint(mCurrentUserCoursesSet, childCoursesSet)) && (!childCoursesSet.contains("default"))){
            System.out.println("MATCHING COURSE!!!!!!!!!!!!!!!!");
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ///////////----------- NEW FILTER --------------//////////////
        final List<String> matchedUsers = new ArrayList<>();
        final String currentUserID = mAuth.getCurrentUser().getUid();
        mMatchedDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    mMatchedDatabase.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabaseUsers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String uid = dataSnapshot.getKey().toString();
                if(!uid.equals(currentUserID)){
                    DatabaseReference userRef = mDatabaseUsers.child(uid);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String courses = dataSnapshot.child("courses").getValue().toString();
                            String name = dataSnapshot.child("name").getValue().toString();
                            String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                            if(checkCourses(courses)){
                                Map matchedMap = new HashMap();
                                matchedMap.put("courses", courses);
                                matchedMap.put("name", name);
                                matchedMap.put("thumb_image", thumbImage);
                                mMatchedDatabase.child(uid).updateChildren(matchedMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                        }
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ///////////----------- NEW FILTER END --------------//////////////
        final HashSet<String> currentUserCourses = new HashSet<>();
        mCurrentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentUserCourses = dataSnapshot.child("courses").getValue().toString();
                StringTokenizer tk = new StringTokenizer(mCurrentUserCourses, "#");
                while(tk.hasMoreTokens()){
                    currentUserCourses.add(tk.nextToken());
                }
                mCurrentUserCoursesSet = currentUserCourses;
                System.out.println("CURRENT USER COURSES: " + mCurrentUserCoursesSet.toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.user_single_layout,
                UsersViewHolder.class,
                //mDatabaseUsers
                mMatchedDatabase
        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setImage(model.getThumb_image(), getApplicationContext());
                viewHolder.setCourses("You've got a match!");
                ////////////////////////////////////////////////////
                String currentUserID = mAuth.getCurrentUser().getUid().toString();
                final String user_id = getRef(position).getKey();
                /*
                viewHolder.setName(model.getName());
                viewHolder.setImage(model.getThumb_image(), getApplicationContext());
                if((checkMatch(model)) && (!user_id.equals(currentUserID))) {// display item
                    viewHolder.setCourses("You've got a match!");
                }else{//don't display item
                    viewHolder.setCourses("No matching courses.");
                    viewHolder.mView.setClickable(false);
                    viewHolder.mView.setEnabled(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.mView.setVisibility(View.GONE);
                        }
                    });
                }
                */
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(SearchActivity.this, BuddyProfileActivity.class);
                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        mUserList.setAdapter(firebaseRecyclerAdapter);
    }

    private boolean checkMatch(Users model) {
        final HashSet<String> childCoursesSet = new HashSet<String>();
        StringTokenizer tk = new StringTokenizer(model.getCourses(), "#");
        while(tk.hasMoreTokens()){
            childCoursesSet.add(tk.nextToken());
        }
        System.out.println("CHILD COURSES: " + childCoursesSet.toString());
        if((!Collections.disjoint(mCurrentUserCoursesSet, childCoursesSet)) && (!childCoursesSet.contains("default"))){
            System.out.println("MATCHING COURSE!!!!!!!!!!!!!!!!");
            return true;
        }
        return false;
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView mUserName = (TextView)mView.findViewById(R.id.textView_name_users);
            mUserName.setText(name);
        }

        public void setCourses(String courses){
            TextView mCourses = (TextView)mView.findViewById(R.id.textView_matchedCourses_users);
            mCourses.setText(courses);
        }

        public void setImage(String image, Context context){
            CircleImageView mImage = (CircleImageView)mView.findViewById(R.id.imageView_displayPicture_users);
            Glide.with(context)
                    .load(image)
                    .into(mImage);
        }
    }
}
