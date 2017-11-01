package com.jxl.studybuddy;

/**
 * Created by Logan on 30/10/2017.
 */

//MSL = Message_Single_Layout AKA the layout XML

        import android.graphics.Color;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import com.google.firebase.auth.FirebaseAuth;

        import java.util.List;

        import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder>{

    private List<Messages> mChatList;
    private FirebaseAuth mAuth;

    public ChatAdapter(List<Messages> mChatList){
        //gets the messages from the console
        //messages is the class
        this.mChatList = mChatList;

    }

    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        //operations to display the layout on the screen
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent,  false);


        return new MessageViewHolder(v);

    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        //locate the message and picture on the right spot

        public TextView textMessage;
        public CircleImageView displayPicture;

        public MessageViewHolder(View view) {
            super (view);
            //mText_box is the textbox ID in MSL_layout
            //profile_picture is the picture ID in MSL_layout

            textMessage = (TextView) view.findViewById(R.id.message_text_view);
            displayPicture = (CircleImageView) view.findViewById(R.id.chat_message_image);
        }
    }

    public void onFindViewHolder(MessageViewHolder viewHolder, int i){
        mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();
        //Messages is from the message class

        Messages c = mChatList.get(i);
        String fromUser = c.getFrom();
        if(fromUser.equals(currentUserID)){
            viewHolder.textMessage.setBackgroundColor(Color.WHITE);
            viewHolder.textMessage.setTextColor(Color.BLACK);
        }else{
            viewHolder.textMessage.setBackgroundResource(R.drawable.text_message_background);
            viewHolder.textMessage.setTextColor(Color.WHITE);
        }
        viewHolder.textMessage.setText(c.getMessage());
    }

    public int getItemCount(){

        return mChatList.size();
    }


}
