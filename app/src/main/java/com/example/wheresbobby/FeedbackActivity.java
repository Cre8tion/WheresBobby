package com.example.wheresbobby;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FeedbackActivity extends AppCompatActivity {
    Dialog myDialog;
    int likeCount = 0;
    int dislikeCount = 0;
    Button likeBtn;
    Button dislikeBtn;
    Button btnComment;
    TextView txtclose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        myDialog = new Dialog(this);
    }

    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.custompopup);
        txtclose = myDialog.findViewById(R.id.txtclose);
        likeBtn = myDialog.findViewById(R.id.btn_like);
        dislikeBtn = myDialog.findViewById(R.id.btn_dislike);
        txtclose.setText("X");
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount += 1;
                likeBtn.setText(String.valueOf(likeCount));
            }
        });
        dislikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeCount -= 1;
                dislikeBtn.setText(String.valueOf(dislikeCount));
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
}