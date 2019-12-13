package com.example.wheresbobby;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

public class Dashboard extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;

    private ActionBarDrawerToggle drawerToggle;
    private FirestoreRecyclerAdapter<FeedbackModel, FeedbackViewHolder> adapter;

    Button TrendingFeedbackButton, FeedbackHistoryButton, LatestFeedbackButton;
    FloatingActionButton MapsButton;
    RecyclerView recyclerView;
    TextView welcome_text;

    private FirebaseAuth mAuth;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Utils.remindOnline(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();

        mDrawer.addDrawerListener(drawerToggle);

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
        setTitle("Home");

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);
        FeedbackHistoryButton = findViewById(R.id.FeedbackHistoryButton);
        TrendingFeedbackButton = findViewById(R.id.TrendingFeedbackButton);
        LatestFeedbackButton = findViewById(R.id.LatestFeedbackButton);
        MapsButton = (FloatingActionButton) findViewById(R.id.MapsButton);
        welcome_text = findViewById(R.id.welcome_text);

        preferences = getSharedPreferences(getResources().getString(R.string.pref_file), MODE_PRIVATE);
        String user_email = preferences.getString("email", "jenny_@");
        String[] temp = user_email.split("_");
        String user_name = temp[0].substring(0, 1).toUpperCase() + temp[0].substring(1);
        welcome_text.setText("Welcome back, " + user_name + "!");

        FeedbackHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                Query query = rootRef.collection("feedbacks").whereEqualTo("poster", user.getEmail().toString())
                        .orderBy("timestamp", Query.Direction.DESCENDING).limit(10);

                FirestoreRecyclerOptions<FeedbackModel> HistoryOptions = new FirestoreRecyclerOptions.Builder<FeedbackModel>()
                        .setQuery(query, FeedbackModel.class)
                        .build();

                adapter.updateOptions(HistoryOptions);

            }
        });

        TrendingFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();

                //LocalDate date = LocalDate.now().minusDays(30);
                //Date beginningDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

                Query query = rootRef.collection("feedbacks")//.whereGreaterThanOrEqualTo("timestamp",beginningDate)
                        .orderBy("timestamp", Query.Direction.DESCENDING).orderBy("likes_count", Query.Direction.DESCENDING).limit(10);

                FirestoreRecyclerOptions<FeedbackModel> HistoryOptions = new FirestoreRecyclerOptions.Builder<FeedbackModel>()
                        .setQuery(query, FeedbackModel.class)
                        .build();

                adapter.updateOptions(HistoryOptions);

            }
        });

        LatestFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                Query query = rootRef.collection("feedbacks")
                        .orderBy("timestamp", Query.Direction.DESCENDING).limit(10);

                FirestoreRecyclerOptions<FeedbackModel> HistoryOptions = new FirestoreRecyclerOptions.Builder<FeedbackModel>()
                        .setQuery(query, FeedbackModel.class)
                        .build();

                adapter.updateOptions(HistoryOptions);

            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        Query query = rootRef.collection("feedbacks")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<FeedbackModel> options = new FirestoreRecyclerOptions.Builder<FeedbackModel>()
                .setQuery(query, FeedbackModel.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<FeedbackModel, FeedbackViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position, @NonNull FeedbackModel model) {
                holder.setFeedback(model.getFeedback());
                holder.setTotalLikes(model.getLikes_Count());
                holder.setTotalDislikes(model.getDislikes_Count());
            }

            @NonNull
            @Override
            public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_layout, parent, false);
                return new FeedbackViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);

        MapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Dashboard.this, MapsActivity.class);
                startActivity(mapIntent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private View view;

        FeedbackViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setFeedback(String feedback) {
            TextView feedbacktextView = view.findViewById(R.id.dashBoardFeedback);
            feedbacktextView.setText(feedback);
        }

        void setUsername(String username){
            TextView usernametextView = view.findViewById(R.id.dashBoardUsername);
            usernametextView.setText(username);
        }

        void setTotalLikes(Integer TotalLikes){
            TextView totallikesView = view.findViewById(R.id.dashBoardLikeCount);
            totallikesView.setText(String.valueOf(TotalLikes));
        }

        void setTotalDislikes(Integer TotalDislikes){
            TextView totalDislikesView = view.findViewById(R.id.dashBoardDislikeCount);
            Log.d("DISLIKES:", String.valueOf(TotalDislikes));
            totalDislikesView.setText(String.valueOf(TotalDislikes));
        }
    }

    private ActionBarDrawerToggle setupDrawerToggle(){
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {

        switch(menuItem.getItemId()) {
            case R.id.home:
                break;
            case R.id.profile:
                /*
                Intent intent = new Intent(Dashboard.this,ProfileActivity.class);
                startActivity(intent);
                 */
                break;
            case R.id.map:
                Intent mapIntent = new Intent(Dashboard.this, MapsActivity.class);
                startActivity(mapIntent);
            default:
                break;
        }


        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }
}
