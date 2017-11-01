package com.jxl.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUserID;
    private String mChatUserName;
    private String mThumbImage;
    private Toolbar mChatToolbar;
    private TextView mChatBarName;
    private TextView mChatBarSeen;
    private ImageView mMessageImage;
    private CircleImageView mChatBarImage;
    private ImageButton mChatAddButton;
    private EditText mChatText;
    private ImageButton mSendButton;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mSwipeLayout;
    private final List<Messages> listOfMessages = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private DatabaseReference mMessageDatabase;
    private static final int NUM_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPos;
    private String mLastKey = "";
    private String mPreviousKey = "";
    private static final int GALLERY_PICK = 1;
    private StorageReference mStorageImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid().toString();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mChatToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);        mAdapter = new MessageAdapter(listOfMessages);
        mMessagesList = (RecyclerView)findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);
        mSwipeLayout = (SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        mChatUserName = getIntent().getStringExtra("user_name");
        mChatUserID = getIntent().getStringExtra("user_id");
        mThumbImage = getIntent().getStringExtra("thumb_image");
        //mChatToolbar.setTitle(mChatUserName);
        mChatBarName = (TextView)findViewById(R.id.chat_bar_userName);
        mChatBarSeen = (TextView)findViewById(R.id.chat_bar_seen);
        mChatBarImage = (CircleImageView)findViewById(R.id.chat_bar_image);
        mChatAddButton = (ImageButton)findViewById(R.id.chat_add_button);
        mChatText = (EditText) findViewById(R.id.chat_edit_text);
        mSendButton = (ImageButton)findViewById(R.id.chat_send_button);
        mMessageImage = (ImageView)findViewById(R.id.message_image_view);
        itemPos = 0;
        mStorageImage = FirebaseStorage.getInstance().getReference();
        mChatBarName.setText(mChatUserName);
        Glide.with(getApplicationContext())
                .load(mThumbImage)
                .into(mChatBarImage);
        loadMessages();
        mRootRef.child("Users").child(mChatUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                if(online.equals("true")){
                    mChatBarSeen.setText("Online");
                }else{
                    GetTimeAgo timeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = timeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mChatBarSeen.setText(lastSeenTime);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mRootRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUserID)){
                    Map chatAddMap = new HashMap<>();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("time_stamp", ServerValue.TIMESTAMP);
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserID + "/" + mChatUserID, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUserID + "/" + mCurrentUserID, chatAddMap);
                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mChatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select image"), GALLERY_PICK);
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                //listOfMessages.clear();
                loadMoreMessages();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent buddiesIntent = new Intent(ChatActivity.this, BuddiesActivity.class);
                startActivity(buddiesIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child(mChatUserID);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(NUM_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                if(!mPreviousKey.equals(messageKey)){
                    listOfMessages.add(itemPos++, message);
                }else{
                    mPreviousKey = mLastKey;
                }
                if(itemPos == 1){
                    mLastKey = messageKey;
                }
                mAdapter.notifyDataSetChanged();
                mSwipeLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);
            }
            //Log.d("KEYS", "LAST KEY: " + mLastKey + "\nPREVIOUS KEY: " + mPreviousKey);
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
    }

    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child(mChatUserID);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * NUM_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if(itemPos == 1){
                    mLastKey = dataSnapshot.getKey();
                    mPreviousKey = dataSnapshot.getKey();
                }
                listOfMessages.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(listOfMessages.size() - 1);
                mSwipeLayout.setRefreshing(false);
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

    }

    private void sendMessage() {
        String message = mChatText.getText().toString();
        System.out.println("MESSAGE: " + message);
        if(!TextUtils.isEmpty(message)){
            String currentUserRef = "messages/" + mCurrentUserID + "/" + mChatUserID;
            String chatUserRef = "messages/" + mChatUserID + "/" + mCurrentUserID;
            DatabaseReference userMessagePush = mRootRef.child("messages").child(mCurrentUserID).child(mChatUserID).push();
            String pushID = userMessagePush.getKey();
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushID, messageMap);
            messageUserMap.put(chatUserRef + "/" + pushID, messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
        }
        mChatText.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final String currentUserRef = "messages/" + mCurrentUserID + "/" + mChatUserID;
            final String chatUserRef = "messages/" + mChatUserID + "/" + mCurrentUserID;
            DatabaseReference userMessagePush = mRootRef.child("messages").child(mCurrentUserID).child(mChatUserID).push();
            final String pushID = userMessagePush.getKey();
            StorageReference filepath = mStorageImage.child("message_images").child(pushID + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        String downloadURL = task.getResult().getDownloadUrl().toString();
                        Map messageMap = new HashMap();
                        messageMap.put("message", downloadURL);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserID);
                        Map messageUserMap = new HashMap();
                        messageUserMap.put(currentUserRef + "/" + pushID, messageMap);
                        messageUserMap.put(chatUserRef + "/" + pushID, messageMap);
                        mChatText.setText("");
                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null){
                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                }
                            }
                        });

                    }
                }
            });
        }
    }
}
