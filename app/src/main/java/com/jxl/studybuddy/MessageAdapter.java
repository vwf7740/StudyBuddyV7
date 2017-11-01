package com.jxl.studybuddy;

/**
 * Created by Xavier on 1/11/2017.
 */

import android.content.Context;
import android.graphics.Color;
import android.os.Message;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Xavier.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private static final int SENT = 1;
    private static final int RECEIVED = 2;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = (Messages) mMessageList.get(position);
        if (message.getFrom().equals(mAuth.getCurrentUser().getUid())) {
            System.out.println("TYPE IS SENT");
            return SENT;
        } else {
            return RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == SENT){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_sent_single_layout, parent, false);
            return new SentMessageViewHolder(v);
        }else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout, parent, false);
            return new MessageViewHolder(v);
        }
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public TextView messageTime;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text_view);
            profileImage = (CircleImageView) view.findViewById(R.id.chat_message_image);
            displayName = (TextView) view.findViewById(R.id.message_userName_view);
            messageTime = (TextView) view.findViewById(R.id.message_time_view);
            messageImage = (ImageView) view.findViewById(R.id.message_image_view);
        }
        public void bind(Messages c, final MessageViewHolder viewHolder){
            String from_user = c.getFrom();
            String messageType = c.getType();
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();
                    //String time = dataSnapshot.child("time").getValue().toString();
                    viewHolder.displayName.setText(name);
                    Glide.with(viewHolder.profileImage.getContext())
                            .load(image)
                            .into(viewHolder.profileImage);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            if(messageType.equals("text")){
                viewHolder.messageImage.setVisibility(View.GONE);
                viewHolder.messageText.setText(c.getMessage());
                viewHolder.messageText.setVisibility(View.VISIBLE);
            }else{
                viewHolder.messageText.setText("shared an image...");
                Glide.with(viewHolder.messageImage.getContext())
                        .load(c.getMessage())
                        .into(viewHolder.messageImage);
                viewHolder.messageImage.setVisibility(View.VISIBLE);
            }
        }
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public TextView messageTime;
        public ImageView messageImage;

        public SentMessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.sent_message_text_view);
            profileImage = (CircleImageView) view.findViewById(R.id.sent_chat_message_image);
            displayName = (TextView) view.findViewById(R.id.sent_message_userName_view);
            messageTime = (TextView) view.findViewById(R.id.sent_message_time_view);
            messageImage = (ImageView) view.findViewById(R.id.sent_message_image_view);
        }

        public void bind(Messages c, final SentMessageViewHolder viewHolder){
            String from_user = c.getFrom();
            String messageType = c.getType();
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
            mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                //String time = dataSnapshot.child("time").getValue().toString();
                viewHolder.displayName.setText(name);
                Glide.with(viewHolder.profileImage.getContext())
                        .load(image)
                        .into(viewHolder.profileImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
            if(messageType.equals("text")){
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageText.setVisibility(View.VISIBLE);
        }else{
            viewHolder.messageText.setText("shared an image...");
            Glide.with(viewHolder.messageImage.getContext())
                    .load(c.getMessage())
                    .into(viewHolder.messageImage);
            viewHolder.messageImage.setVisibility(View.VISIBLE);
        }
    }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
        Messages c = mMessageList.get(i);

        switch (viewHolder.getItemViewType()) {
            case SENT:
                SentMessageViewHolder viewHolderSent = (SentMessageViewHolder)viewHolder;
                viewHolderSent.bind(c, viewHolderSent);
                break;
            case RECEIVED:
                MessageViewHolder viewHolderReceived = (MessageViewHolder)viewHolder;
                viewHolderReceived.bind(c, viewHolderReceived);
        }
        /*String from_user = c.getFrom();
        String messageType = c.getType();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                //String time = dataSnapshot.child("time").getValue().toString();
                viewHolder.displayName.setText(name);
                Glide.with(viewHolder.profileImage.getContext())
                        .load(image)
                        .into(viewHolder.profileImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        if(messageType.equals("text")){
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageText.setVisibility(View.VISIBLE);
        }else{
            viewHolder.messageText.setText("shared an image...");
            Glide.with(viewHolder.messageImage.getContext())
                    .load(c.getMessage())
                    .into(viewHolder.messageImage);
            viewHolder.messageImage.setVisibility(View.VISIBLE);
        }
        */
    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}