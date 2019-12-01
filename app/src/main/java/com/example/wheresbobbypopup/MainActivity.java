package com.example.wheresbobbypopup;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_main);
        myDialog = new Dialog(this);
    }

    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.custompopup);
        txtclose = myDialog.findViewById(R.id.txtclose);
        btnComment = myDialog.findViewById(R.id.btn_comment);
        txtclose.setText("X");
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount += 1;
                btnComment.setText(String.valueOf(likeCount));
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
}