package com.example.wheresbobby;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;


public class CommentModel {
    private String text, user;

    public CommentModel() {}

    public CommentModel(String text, String user) {
        this.text = text;
        this.user = user;
    }
    @PropertyName("text")
    public String getComment() {return text;}

    public String getText() {return text;}

    @PropertyName("user")
    public String getUsername(){
        return user;
    }

}
