package com.example.wheresbobby;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.HashMap;

public class FeedbackModel {
    private String feedback, poster;
    private Integer area_id, timestamp, likes_count, dislikes_count;

    public FeedbackModel() {}

    public FeedbackModel(String feedback, String poster, Integer area_id, Integer timestamp, Integer likes_count, Integer dislikes_count) {
        this.feedback = feedback;
        this.poster = poster;
        this.timestamp = timestamp;
        this.area_id = area_id;
        this.likes_count = likes_count;
        this.dislikes_count = dislikes_count;
    }

    public String getFeedback() {return feedback;}

    public String getPoster(){
        return poster;
    }

    public int getLikes_Count(){
        return likes_count;
    }

    public int getDislikes_Count(){
        return dislikes_count;
    }


}
