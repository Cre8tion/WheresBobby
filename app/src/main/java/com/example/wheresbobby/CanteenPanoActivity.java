package com.example.wheresbobby;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CanteenPanoActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean addbobby = false;
    private float[] lastTouchDownXY = new float[2];
    private int bobbyindex;
    private ImageView campuscenter;
    private ConstraintLayout constrain;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DocumentReference indexReference;

    private String currentFeedback;
    RecyclerView commentsView;
    FirestoreRecyclerAdapter<CommentModel, CommentViewHolder> adapter;

    Dialog myDialog;
    Dialog newDialog;
    int likeCount = 0;
    int dislikeCount = 0;
    Button likeBtn;
    Button dislikeBtn;
    TextView txtclose;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen_pano);

        Utils.remindOnline(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myDialog = new Dialog(CanteenPanoActivity.this);
        newDialog = new Dialog(CanteenPanoActivity.this);

        campuscenter = findViewById(R.id.campuscenter);
        constrain = findViewById(R.id.constrain);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

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
                    addNewFeedback();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseCall();
    }

    public void firebaseCall(){
        getMaxIndex();
    }

    public void addNewFeedback(){
        myDialog.setContentView(R.layout.row_comment);

        Button PostFeedbackButton = myDialog.findViewById(R.id.PostFeedBackbutton);

        TextView comment_username = myDialog.findViewById(R.id.comment_username);
        comment_username.setText(user.getEmail());

        final EditText comment_content = myDialog.findViewById(R.id.comment_content);
        PostFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feedback = comment_content.getText().toString();
                try {
                    URL url = Utils.buildURL();
                    JSONObject json = new JSONObject();
                    json.put("comment",feedback);

                    currentFeedback = feedback;

                    new profanityCheck().execute(url.toString(),json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

    }

    public void feedBackCreated(){
        createBobby(campuscenter,constrain);
        bobbyindex += 1;

        indexReference.update("maxIndex",bobbyindex).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Update", "DocumentSnapshot successfully updated!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Update", "Error updating document", e);
                    }
                });

        addbobby = false;
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

        bobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CanteenPanoActivity.this, bobby.getId() + " bobby is clicked", Toast.LENGTH_SHORT).show();
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
        });
        constrain.addView(bobby);
    }

    public void getMaxIndex() {
        Query query = db.collection("areas").whereEqualTo("id",0);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                       bobbyindex = Integer.parseInt(document.getData().get("maxIndex").toString());
                       indexReference = document.getReference();
                       Log.d("jiayue", "DocumentSnapshot data: " + bobbyindex);
                       setBobbys();
                    }
                } else {
                    Log.d("ERROR", "Error getting documents: ", task.getException());
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

                                Log.d("position X =>",bobbypositions[0]);
                                Log.d("position Y =>",bobbypositions[1]);

                                createBobby(constrain,bobbypositions);
                            }
                        } else {
                            Log.d("Error", "" + task.getException());
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

        bobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CanteenPanoActivity.this, bobby.getId() + " bobby is clicked", Toast.LENGTH_SHORT).show();
                newDialog.setContentView(R.layout.custompopup);

                txtclose = newDialog.findViewById(R.id.txtclose);
                likeBtn = newDialog.findViewById(R.id.btn_like);
                dislikeBtn = newDialog.findViewById(R.id.btn_dislike);
                txtclose.setText("X");
                txtclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newDialog.dismiss();
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
                newDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                newDialog.show();

                commentsView = (RecyclerView) newDialog.findViewById(R.id.recyclerCommentsView);
                commentsView.setHasFixedSize(true);
                commentsView.setLayoutManager(new LinearLayoutManager(newDialog.getContext()));

                Query query = db.collection("comments").whereEqualTo("feedback_id", bobby.getId()).orderBy("timestamp", Query.Direction.DESCENDING);

                FirestoreRecyclerOptions<CommentModel> options = new FirestoreRecyclerOptions.Builder<CommentModel>()
                        .setQuery(query, CommentModel.class)
                        .build();

                adapter = new FirestoreRecyclerAdapter<CommentModel, CommentViewHolder>(options) {

                    @Override
                    protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull CommentModel model) {
                        holder.setFeedback(model.getComment());
                        holder.setUsername(model.getUsername());
                    }

                    @NonNull
                    @Override
                    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_cards_layout, parent, false);
                        return new CommentViewHolder(view);
                    }
                };
                commentsView.setAdapter(adapter);
            }
        });

        constrain.addView(bobby);
    }


    private class profanityCheck extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                OutputStreamWriter out = new OutputStreamWriter(httpURLConnection.getOutputStream());
                out.write(params[1]);
                out.close();

                int statusCode = httpURLConnection.getResponseCode();

                if (statusCode == 200) {
                    InputStream it = new BufferedInputStream(httpURLConnection.getInputStream());
                    InputStreamReader read = new InputStreamReader(it);
                    BufferedReader buff = new BufferedReader(read);
                    StringBuilder dta = new StringBuilder();
                    String chunks;
                    while ((chunks = buff.readLine()) != null) {
                        dta.append(chunks);
                    }
                    String returndata = dta.toString();
                    return returndata;
                } else {
                    Log.e("ERROR", "no output");
                    return "Invalid Status Code";
                }

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Error Occurred";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("TAG", result);
            if(result.equals("1")){
                Toast.makeText(CanteenPanoActivity.this, "No Profanity is Allowed!", Toast.LENGTH_LONG).show();
            }
            else if(result.equals("0")){
                feedBackCreated();
                myDialog.dismiss();
            }
            else if(result.equals("-1")){
                Toast.makeText(CanteenPanoActivity.this, "An error has occurred.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CommentViewHolder extends RecyclerView.ViewHolder {
        private View view;

        CommentViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setFeedback(String feedback) {
            TextView feedbacktextView = view.findViewById(R.id.commentFeedback);
            feedbacktextView.setText(feedback);
        }

        void setUsername(String username){
            TextView usernametextView = view.findViewById(R.id.commentUsername);
            usernametextView.setText(username);
        }
    }
}
