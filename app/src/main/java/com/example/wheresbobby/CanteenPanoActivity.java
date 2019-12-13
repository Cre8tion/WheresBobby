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
import com.google.firebase.firestore.FieldValue;
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
import org.w3c.dom.Document;

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
import java.util.Arrays;
import java.util.Date;
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
    private int likes_amount;
    private int dislikes_amount;

    private boolean user_likes;
    private boolean user_dislikes;

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
    TextView feedback_text;
    TextView dislike_text;
    TextView like_text;


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

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle("Campus Center");

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

    /*@Override
    protected void onStop(){
        super.onStop();
        adapter.stopListening();
    }*/

    /***
     * Helper Function to start the Firebase calls
     */
    public void firebaseCall(){
        getMaxIndex();
    }


    /***
     * Function to call when trying to create a new feedback, which will call a AsyncTask to check
     * if there is any profanity found, which only allow users to post feedback accordingly
     */
    public void addNewFeedback(){
        myDialog.setContentView(R.layout.row_comment);

        Button PostFeedbackButton = myDialog.findViewById(R.id.PostFeedBackbutton);
        TextView comment_username = myDialog.findViewById(R.id.comment_username);
        comment_username.setText("Posting as anonymous");

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

    /***
     * Helper function called to increase the maxIndex in Firebase by 1 and call function to add
     * to the View
     */
    public void feedBackCreated(){

        indexReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                bobbyindex = Integer.parseInt(documentSnapshot.getData().get("maxIndex").toString());
                addOneBobby(campuscenter,constrain);
                bobbyindex += 1;
                indexReference.update("maxIndex", bobbyindex);
            }
        });

        addbobby = false;
    }

    /***
     * Function to call a loop to populate the Layout with multiple ImageButtons
     */
    public void setBobbys(){
        for (int i=0;i<bobbyindex;i++){
            getBobbyposition(i);
        }
    }

    /***
     * Function to convert dp to pixel for storage purposes
     * @param dp value of dp
     * @param context Current Activity
     * @return value in pixel
     */
    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /***
     * Function to add a new ImageView to the panorama after feedback is posted and store the
     * details in Firebase
     * @param v Imageview to add to View
     * @param constrain Panorama Layout for Imageview to be added to
     */

    public void addOneBobby(ImageView v, ConstraintLayout constrain){
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
        position.put("poster", user.getEmail());
        position.put("likes_count", 0);
        position.put("dislikes_count", 0);
        // Store the post ids that a person has liked in their users tab
        Date now = new Date();
        Long time = new Long(now.getTime()/1000);
        int timestamp = time.intValue();
        position.put("timestamp", timestamp);
        position.put("feedback", currentFeedback);
        position.put("area_id", 0); // AREA ID HARDCODED!!!
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

        constrain.addView(bobby);
        getBobbyposition(bobbyindex);
    }

    /***
     * Get the maximum number of feedbacks available to start adding them to the View
     */

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

    /***
     * Retrieve Firebase data using index to search for document with matching id
     * @param index id of feedback to retrieve
     */

    public void getBobbyposition(int index){
        db.collection("feedbacks")
                .whereEqualTo("id",index)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String[] bobbypositions = new String[7];

                                bobbypositions[0] = document.getData().get("positionX").toString();
                                bobbypositions[1] = document.getData().get("positionY").toString();
                                bobbypositions[2] = document.getData().get("id").toString();
                                bobbypositions[3] = document.getData().get("feedback").toString();
                                bobbypositions[4] = document.getData().get("likes_count").toString();
                                bobbypositions[5] = document.getData().get("dislikes_count").toString();
                                bobbypositions[6] = document.getData().get("id").toString();
                                // if want to show poster, just getdata poster and put in [7]

                                Log.d("position X =>",bobbypositions[0]);
                                Log.d("position Y =>",bobbypositions[1]);

                                showBobby(constrain,bobbypositions);
                            }
                        } else {
                            Log.d("Error", "" + task.getException());
                        }

                    }
                });
    }

    /***
     * Add new ImageButtons of Feedbacks to Layout of Panoramic view of Canteen
     * @param constrain Layout to add new ImageButtons
     * @param bobbypositions Array of document details to populate the ImageButton with
     *                       including positions to set the ImageButton
     */

    public void showBobby( ConstraintLayout constrain, String[] bobbypositions){
        // This function makes the bobby popup
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT); //WRAP_CONTENT param can be FILL_PARENT
        params.leftMargin = Integer.parseInt(bobbypositions[0]);
        params.topMargin = Integer.parseInt(bobbypositions[1]);
        params.leftToLeft = constrain.getId();
        params.topToTop = constrain.getId();
        params.height = (int) convertDpToPixel(75, CanteenPanoActivity.this);
        params.width = (int) convertDpToPixel(75, CanteenPanoActivity.this);

        newDialog = new Dialog(CanteenPanoActivity.this);
        final ImageButton bobby = new ImageButton(CanteenPanoActivity.this);
        final String feedback_content = bobbypositions[3];
        likes_amount = Integer.parseInt(bobbypositions[4]);
        dislikes_amount = Integer.parseInt(bobbypositions[5]);
        bobbyindex = Integer.parseInt(bobbypositions[6]);
        bobby.setLayoutParams(params);
        Drawable bobbyhead = getResources().getDrawable(R.drawable.bobby_burned);
        bobby.setImageDrawable(bobbyhead);
        bobby.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bobby.setBackground(null);
        bobby.setId(Integer.parseInt(bobbypositions[2]));

        // (if not already done)
        bobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bobbyindex = bobby.getId();
                update_bools(bobbyindex);

                DocumentReference post_ref = db.collection("feedbacks").document(Integer.toString(bobbyindex));
                post_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        likes_amount = Integer.parseInt(documentSnapshot.getData().get("likes_count").toString());
                        dislikes_amount = Integer.parseInt(documentSnapshot.getData().get("dislikes_count").toString());
                    }
                });

                // Toast.makeText(CanteenPanoActivity.this, bobby.getId() + " bobby is clicked", Toast.LENGTH_SHORT).show();
                newDialog.setContentView(R.layout.custompopup);

                txtclose = newDialog.findViewById(R.id.txtclose);
                likeBtn = newDialog.findViewById(R.id.btn_like);
                dislikeBtn = newDialog.findViewById(R.id.btn_dislike);
                like_text = newDialog.findViewById(R.id.like_count_view);
                dislike_text = newDialog.findViewById(R.id.dislike_count_view);
                feedback_text = newDialog.findViewById(R.id.feedback_display);

                Button add_comment = newDialog.findViewById(R.id.confirmComment);
                like_text.setText(String.valueOf(likes_amount));
                dislike_text.setText(String.valueOf(dislikes_amount));

                feedback_text.setText(feedback_content);
                Log.d("Feedback text set", feedback_content);
                txtclose.setText("X");
                txtclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newDialog.dismiss();
                    }
                });

                final DocumentReference like_ref = db
                        .collection("feedbacks").document(Integer.toString(bobbyindex))
                        .collection("likes_users").document(user.getEmail());
                final DocumentReference dislike_ref = db
                        .collection("feedbacks").document(Integer.toString(bobbyindex))
                        .collection("dislikes_users").document(user.getEmail());

                likeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // first check if user alr liked this
                        if(!user_likes){
                            likes_amount += 1;
                            like_text.setText(String.valueOf(likes_amount));
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put("likes_count", likes_amount);
                            db.collection("feedbacks").document(Integer.toString(bobbyindex))
                                    .set(data, SetOptions.merge());
                            data.clear();
                            data.put("value", "1");
                            like_ref.set(data, SetOptions.merge());
                            user_likes = true;
                            if(user_dislikes){
                                dislikes_amount -= 1;
                                dislike_text.setText(String.valueOf(dislikes_amount));
                                data.clear();
                                data.put("dislikes_count", dislikes_amount);
                                db.collection("feedbacks").document(Integer.toString(bobbyindex))
                                        .set(data, SetOptions.merge());
                                data.clear();
                                data.put("value", "0");
                                dislike_ref.set(data, SetOptions.merge());
                                user_dislikes = false;
                            }
                        }
                        update_bools(bobbyindex);
                    }
                });
                dislikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!user_dislikes){
                            dislikes_amount += 1;
                            dislike_text.setText(String.valueOf(dislikes_amount));
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put("dislikes_count", dislikes_amount);
                            db.collection("feedbacks").document(Integer.toString(bobbyindex))
                                    .set(data, SetOptions.merge());
                            data.clear();
                            data.put("value", "1");
                            dislike_ref.set(data, SetOptions.merge());
                            user_dislikes = true;
                            if (user_likes) {
                                likes_amount -= 1;
                                like_text.setText(String.valueOf(likes_amount));
                                data.clear();
                                data.put("likes_count", likes_amount);
                                db.collection("feedbacks").document(Integer.toString(bobbyindex))
                                        .set(data, SetOptions.merge());
                                data.clear();
                                data.put("value", "0");
                                like_ref.set(data, SetOptions.merge());
                                user_likes = false;
                            }
                        }
                        update_bools(bobbyindex);
                    }
                });

                add_comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final EditText comment_text = newDialog.findViewById(R.id.addComment);
                        String comment = comment_text.getText().toString();
                        if (comment.equals("")) return;
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("id", bobbyindex);
                        data.put("comment", comment);
                        Date now = new Date();
                        Long time = new Long(now.getTime()/1000);
                        int timestamp = time.intValue();
                        data.put("timestamp", timestamp);
                        data.put("poster", user.getEmail());
                        db.collection("comments")
                                .add(data)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        comment_text.setText("");

                                    }
                                });
                    }
                });

                newDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                newDialog.show();

                commentsView = (RecyclerView) newDialog.findViewById(R.id.recyclerCommentsView);
                commentsView.setHasFixedSize(true);
                commentsView.setLayoutManager(new LinearLayoutManager(newDialog.getContext()));

                Query query = db.collection("comments").whereEqualTo("id", bobbyindex)
                        .orderBy("timestamp", Query.Direction.ASCENDING);

                FirestoreRecyclerOptions<CommentModel> options = new FirestoreRecyclerOptions.Builder<CommentModel>()
                        .setQuery(query, CommentModel.class)
                        .build();

                adapter = new FirestoreRecyclerAdapter<CommentModel, CommentViewHolder>(options) {

                    @Override
                    protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull CommentModel model) {
                        Log.d("Pos", String.valueOf(position));
                        holder.setFeedback(model.getComment());
                        // holder.setUsername(model.getPoster());
                    }

                    @NonNull
                    @Override
                    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_cards_layout, parent, false);
                        return new CommentViewHolder(view);
                    }
                };
                commentsView.setAdapter(adapter);
                adapter.updateOptions(options);
                adapter.startListening();
            }
        });

        constrain.addView(bobby);
    }

    /** Assignment for whether or not user likes/dislikes.
     * @param index id of feedback
     */

    void update_bools(int index) {

        DocumentReference like_ref = db
                .collection("feedbacks").document(Integer.toString(index))
                .collection("likes_users").document(user.getEmail());
        DocumentReference dislike_ref = db
                .collection("feedbacks").document(Integer.toString(index))
                .collection("dislikes_users").document(user.getEmail());

        like_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getData().get("value").toString().equals("1")) {
                            user_likes = true;
                        } else {
                            user_likes = false;
                        }
                    } else {
                        user_likes = false;
                    }
                } else {
                    Log.d("ERROR", "get failed with ", task.getException());
                }
            }
        });

        dislike_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getData().get("value").toString().equals("1")) {
                            user_dislikes = true;
                        } else {
                            user_dislikes = false;
                        }
                    } else {
                        user_dislikes = false;
                    }
                }
            }
        });
    }

    /***
     * AsyncTask Class to call API to check if there is any profanity involved in the feedback
     */
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

    /** Represents a Viewholder for Holding Comments on Feedbacks
     *  Holds multiple comments
     */

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

        /*void setUsername(String username){
            TextView usernametextView = view.findViewById(R.id.commentUsername);
            usernametextView.setText(username);
        }*/
    }
}
