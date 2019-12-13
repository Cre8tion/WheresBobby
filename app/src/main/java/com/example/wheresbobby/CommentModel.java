package com.example.wheresbobby;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;


public class CommentModel {
    private String comment;

    public CommentModel() {}

    public CommentModel(String commentr) {
        this.comment = comment;
        // this.poster = poster;
    }

    public String getComment() {return comment;}

    /* public String getPoster(){
        return poster;
    }*/

}
