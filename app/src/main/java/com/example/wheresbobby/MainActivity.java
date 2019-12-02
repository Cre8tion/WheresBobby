package com.example.wheresbobby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String pref_file ="com.example.wheresbobby.sharedprefs";
    private FirebaseAuth mAuth;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(pref_file, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        checkAuth();
    }

    public void checkAuth(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            user.reload();
            Log.d("EMAIL", user.getEmail().toString());
            Log.d("VERIFIED", Boolean.toString(user.isEmailVerified()));
            Log.d("UID", user.getUid());
            Log.d("Image", String.valueOf(user.getPhotoUrl()));
            if (user.isEmailVerified()){
                Log.d("TAG", "Success, already logged in");
                proceed();
                return;
            }else{
                // Toast to ask user to verify email
                Toast.makeText(MainActivity.this, "Email not verified!", Toast.LENGTH_SHORT).show();
                Log.d("LOGIN FAILED", "User logged in but email not verified");
                Intent intent = new Intent(MainActivity.this, EmailSent.class);
                startActivity(intent);
                return;
            }
        }else{
            // Check if there is a stored email and token
            String email = preferences.getString("email", "");
            if (email == ""){
                Log.d("LOGIN FAILED", "No credentials stored");
                return;
            }
            // Attempt login verification with server
            String token = preferences.getString("token", "");
            mAuth.signInWithEmailAndPassword(email, token)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user.isEmailVerified()){
                                    Log.d("TAG", "Success, logged in with stored credentials");
                                    proceed();
                                    return;
                                }else{
                                    Toast.makeText(MainActivity.this, "Email not verified!", Toast.LENGTH_SHORT).show();
                                    Log.d("LOGIN FAILED", "Logged in with local credentials, but mail not verified");
                                    Intent intent = new Intent(MainActivity.this, EmailSent.class);
                                    startActivity(intent);
                                    return;
                                }
                            }else{
                                Log.d("LOGIN FAILED", "Invalid local credentials");
                                Toast.makeText(MainActivity.this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            return;
        }
    }

    public void registerAccount(){
        EditText inp = (EditText) findViewById(R.id.email_input);
        String email = inp.getText().toString();
        String token = getToken();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putString("token", token);
        editor.apply();

        if(!email.isEmpty()) {
            mAuth.createUserWithEmailAndPassword(email, token)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Send user verification email, direct to page
                                FirebaseUser user = mAuth.getCurrentUser();
                                doVerification(user);
                            } else {
                                Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void doVerification(FirebaseUser user){
        // Send verification email to user, then direct them to wait page.
        String email = user.getEmail();
        if (user.isEmailVerified()){
            proceed();
            return;
        }

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Intent intent = new Intent(MainActivity.this, EmailSent.class);
        startActivity(intent);
    }

    public String getToken(){

        int left = 33;
        int right = 126;
        int length = 24;
        Random random = new Random();

        StringBuilder buffer = new StringBuilder(length);
        for(int i=0;i<length;i++){
            int randomInt = left + (int) (random.nextFloat() * (right - left + 1));
            buffer.append((char) randomInt);
        }

        String token = buffer.toString();
        Log.d("TOKEN:", token);
        return token;
    }

    public void proceed(){
        Intent intent = new Intent(MainActivity.this, Dashboard.class);
        startActivity(intent);
    }

    public void loginButton(View view){
        registerAccount();
    }

}
