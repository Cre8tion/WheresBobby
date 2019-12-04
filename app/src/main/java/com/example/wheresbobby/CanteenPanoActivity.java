package com.example.wheresbobby;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class CanteenPanoActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean addbobby = false;
    private float[] lastTouchDownXY = new float[2];
    private int bobbyindex;
    private ImageView campuscenter;
    private ConstraintLayout constrain;

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
        setContentView(R.layout.activity_canteen_pano);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myDialog = new Dialog(this);

        campuscenter = findViewById(R.id.campuscenter);
        constrain = findViewById(R.id.constrain);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CanteenPanoActivity.this, "Bobby is watching you :)", Toast.LENGTH_SHORT).show();
                addbobby = true;
            }
        });

        campuscenter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastTouchDownXY[0] = event.getX();
                    lastTouchDownXY[1] = event.getY();
                }
                return false;
            }
        });

        campuscenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addbobby) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    createBobby(campuscenter,constrain);
                    bobbyindex += 1;

                    Map<String, Object> bobbynumber = new HashMap<String, Object>();
                    bobbynumber.put("bobbyindex",bobbyindex);
                    bobbynumber.put("bobby",true);
                    db
                            .collection("feedbacks")
                            .document("index")
                            .set(bobbynumber);
                    addbobby = false;
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseCall();
    }

    public void firebaseCall(){
        getMaxIndex();
    }

    public void setBobbys(){
        for (int i=0;i<bobbyindex;i++){
            getBobbyposition(i);
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /*public static float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }*/


    public void createBobby(ImageView v, ConstraintLayout constrain){
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT); //WRAP_CONTENT param can be FILL_PARENT

        params.leftMargin = (int) lastTouchDownXY[0] - 90; //XCOORD
        params.topMargin = (int) lastTouchDownXY[1] - 90; //YCOORD
        params.leftToLeft = v.getId();
        params.topToTop = v.getId();
        params.height = (int) convertDpToPixel(75, CanteenPanoActivity.this);
        params.width = (int) convertDpToPixel(75, CanteenPanoActivity.this);

        Map<String, Object> position = new HashMap<String, Object>();
        position.put("positionX", (int) lastTouchDownXY[0] - 90);
        position.put("positionY", (int) lastTouchDownXY[1] - 90);
        position.put("id",bobbyindex);
        position.put("create",true);
        db
                .collection("feedbacks")
                .document(""+bobbyindex)
                .set(position, SetOptions.merge());


        final ImageButton bobby = new ImageButton(CanteenPanoActivity.this);
        bobby.setLayoutParams(params);
        Drawable bobbyhead = getResources().getDrawable(R.drawable.bobby_burned);
        bobby.setImageDrawable(bobbyhead);
        bobby.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bobby.setBackground(null);
        bobby.setId(bobbyindex);
        //bobby.setTag(""+bobbyindex);

        bobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CanteenPanoActivity.this, bobby.getId() + " bobby is clicked", Toast.LENGTH_SHORT).show();
                myDialog.setContentView(R.layout.custompopup);
                txtclose = myDialog.findViewById(R.id.txtclose);
                btnComment = myDialog.findViewById(R.id.btn_comment);
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
        });
        constrain.addView(bobby);
    }

    public void getMaxIndex() {

        DocumentReference docRef = db.collection("feedbacks").document("index");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        bobbyindex = Integer.parseInt(document.getData().get("bobbyindex").toString());
                        Log.d("jiayue", "DocumentSnapshot data: " + bobbyindex);
                        setBobbys();
                    } else {
                        Log.d("jiayue", "No such document");
                    }
                } else {
                    Log.d("jiayue", "get failed with ", task.getException());
                }
            }
        });

    }

    public void getBobbyposition(int index){
        db.collection("feedbacks")
                .whereEqualTo("id",index)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String[] bobbypositions = new String[3];

                                bobbypositions[0] = document.getData().get("positionX").toString();
                                bobbypositions[1] = document.getData().get("positionY").toString();
                                bobbypositions[2] = document.getData().get("id").toString();

                                Log.d("jiayue", document.getId() + " position X => " + bobbypositions[0]);
                                Log.d("jiayue - position Y:",bobbypositions[1]);

                                createBobby(constrain,bobbypositions);
                            }
                        } else {
                            Log.d("jiayue", "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    public void createBobby( ConstraintLayout constrain, String[] bobbypositions){
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT); //WRAP_CONTENT param can be FILL_PARENT
        params.leftMargin = Integer.parseInt(bobbypositions[0]);
        params.topMargin = Integer.parseInt(bobbypositions[1]);
        params.leftToLeft = constrain.getId();
        params.topToTop = constrain.getId();
        params.height = (int) convertDpToPixel(75, CanteenPanoActivity.this);
        params.width = (int) convertDpToPixel(75, CanteenPanoActivity.this);


        final ImageButton bobby = new ImageButton(CanteenPanoActivity.this);
        bobby.setLayoutParams(params);
        Drawable bobbyhead = getResources().getDrawable(R.drawable.bobby_burned);
        bobby.setImageDrawable(bobbyhead);
        bobby.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bobby.setBackground(null);
        bobby.setId(Integer.parseInt(bobbypositions[2]));
        //bobby.setTag(bobbypositions[2]);
        bobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CanteenPanoActivity.this, bobby.getId() + " bobby is clicked", Toast.LENGTH_SHORT).show();
                myDialog.setContentView(R.layout.custompopup);
                txtclose = myDialog.findViewById(R.id.txtclose);
                btnComment = myDialog.findViewById(R.id.btn_comment);
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
        });

        constrain.addView(bobby);
    }

}
