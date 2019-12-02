package com.example.wheresbobby;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.HashMap;

public class FeedbackModel {
    private String text, user;
    private Timestamp timestamp;
    private Integer area_id;
    private ArrayList<HashMap<String, String>> likes, dislikes;

    public FeedbackModel() {}

    public FeedbackModel(String text, String user, Timestamp timestamp, Integer area_id, ArrayList<HashMap<String, String>> likes, ArrayList<HashMap<String, String>> dislikes) {
        this.text = text;
        this.user = user;
        this.timestamp = timestamp;
        this.area_id = area_id;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public String getFeedback() {return text;}

    public String getText() {return text;}


    @PropertyName("user")
    public String getUsername(){
        return user;
    }

    @PropertyName("likes")
    public ArrayList<HashMap<String, String>> getLikes(){
        return likes;
    }

    @PropertyName("dislikes")
    public ArrayList<HashMap<String,String>> getDislikes(){
        return dislikes;
    }


}
