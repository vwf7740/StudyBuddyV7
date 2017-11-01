package com.jxl.studybuddy;

/**
 * Created by Logan on 24/09/2017.
 */

public class ChatMessage
{
    String user;
    String message;

    public ChatMessage(String aMessage, String aUser)
    {
        this.message = aMessage;
        this.user = aUser;
    }

    public String getChatMessageUser()
    {
        return user;
    }

    public void setChatMessageUser(String aUser)
    {
        this.user = aUser;
    }


    public String getChatMessage()
    {
        return message;
    }

    public void setChatMessage(String aMessage)
    {
        this.message = aMessage;
    }

}
