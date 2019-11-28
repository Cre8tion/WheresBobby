package com.example.wheresbobby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailSent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sent);
    }

    public void emailCheck(View view){
        Intent intent = new Intent(EmailSent.this, MainActivity.class);
        startActivity(intent);
    }

    public void sendEmail(View view){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            String email = user.getEmail();
            if (user.isEmailVerified()){
                Intent intent = new Intent(EmailSent.this, Dashboard.class);
                return;
            }

            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(EmailSent.this, "Email sent.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }else{
            Intent intent = new Intent(EmailSent.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
