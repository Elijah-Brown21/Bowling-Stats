package com.example.bowlingstats;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class NewSessionActivity extends AppCompatActivity {

    private TextView dateTextView;
    private EditText editTextG1;
    private EditText editTextG2;
    private EditText editTextG3;
    private Button submitButton;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_new_session);

        dateTextView = findViewById(R.id.view_date);
        editTextG1 = findViewById(R.id.edit_g1);
        editTextG2 = findViewById(R.id.edit_g2);
        editTextG3 = findViewById(R.id.edit_g3);
        submitButton = findViewById(R.id.submit_button);

        Intent incomingIntent = getIntent();
        String date = incomingIntent.getStringExtra("date");
        dateTextView.setText(date);

        Date timestamp;
        try {
            timestamp = new SimpleDateFormat("MM/dd/yyyy").parse(date);
        } catch (ParseException e) {
            timestamp = new GregorianCalendar(0,0,0).getTime();
            e.printStackTrace();
        }

        Date finalTimestamp = timestamp;
        submitButton.setOnClickListener((v) -> {
                    String g1Text = editTextG1.getText().toString();
                    String g2Text = editTextG2.getText().toString();
                    String g3Text = editTextG3.getText().toString();

                    if (g1Text.matches("") || g2Text.matches("") || g3Text.matches("")) {
                        Toast.makeText(NewSessionActivity.this, "Please enter a valid score", Toast.LENGTH_SHORT).show();
                    } else {
                        int game1 = Integer.parseInt(g1Text);
                        int game2 = Integer.parseInt(g2Text);
                        int game3 = Integer.parseInt(g3Text);

                        boolean game1Val = validateGame(game1);
                        boolean game2Val = validateGame(game2);
                        boolean game3Val = validateGame(game3);

                        if (!date.equals("") && game1Val && game2Val && game3Val) {
                            Session session = new Session(date, finalTimestamp, game1, game2, game3);
                            uploadSession(session);
                            Intent intent = new Intent("finish_activity");
                            sendBroadcast(intent);
                            Intent openMainActivity = new Intent(NewSessionActivity.this, MainActivity.class);
                            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityIfNeeded(openMainActivity, 0);
                        }
                    }
                });
    }

    public void uploadSession(Session session) {
        db.collection(currentUser.getEmail())
                .add(session)
                .addOnSuccessListener((OnSuccessListener) (documentReference) -> {
                    Toast.makeText(NewSessionActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener((e) -> {
                    Toast.makeText(NewSessionActivity.this, "Error", Toast.LENGTH_SHORT).show();
                });
    }

    public boolean validateGame(int game) {
        if (game < 0 || game > 300) {
            Toast.makeText(NewSessionActivity.this, "Please enter a valid score", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}