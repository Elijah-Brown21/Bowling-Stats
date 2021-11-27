package com.example.bowlingstats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class StatsPageActivity extends AppCompatActivity {

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference sessionRef = db.collection(currentUser.getEmail());
    private ArrayList<Session> sessions = new ArrayList<>();
    TextView overallAverage, sessionAverage, game1Average, game2Average, game3Average;
    TextView overallTotalPins, totalGames, totalSessions;
    Button listBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserTitle();
        setContentView(R.layout.activity_stats_page);

        mAuth = FirebaseAuth.getInstance();

        overallAverage = findViewById(R.id.text_view_overall_average);
        sessionAverage = findViewById(R.id.text_view_session_average);
        game1Average = findViewById(R.id.game1_view);
        game2Average = findViewById(R.id.game2_view);
        game3Average = findViewById(R.id.game3_view);
        overallTotalPins = findViewById(R.id.text_view_overall_total);
        totalGames = findViewById(R.id.total_games_view);
        totalSessions = findViewById(R.id.total_sessions_view);


        listBtn = findViewById(R.id.list_button);

        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatsPageActivity.this, MainActivity.class));
//                sessions.clear();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_btn_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_btn:
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(StatsPageActivity.this);
                confirmDialog.setTitle("Logout?");
                confirmDialog.setMessage("You will be required to enter your email and password for re-entry.");
                confirmDialog.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        startActivity(new Intent(StatsPageActivity.this, LoginActivity.class));
                        finish();
                    }
                });

                confirmDialog.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                confirmDialog.show();
                return true;
            default:
                return true;
        }
    }

    protected void onStart() {
        super.onStart();
        sessions.clear();
        sessionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(StatsPageActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Session session = documentSnapshot.toObject(Session.class);
                    sessions.add(session);
                }

                overallAverage.setText("Per Game..................." + String.valueOf(getUserPerGameAverage(sessions)));
                sessionAverage.setText("Session......................" + String.valueOf(getUserSessionAverage(sessions)));
                game1Average.setText("Game 1......................" + String.valueOf(getUserGameAverage(sessions, 1)));
                game2Average.setText("Game 2......................" + String.valueOf(getUserGameAverage(sessions, 2)));
                game3Average.setText("Game 3......................" + String.valueOf(getUserGameAverage(sessions, 3)));

                overallTotalPins.setText("Pins........................." + String.valueOf(getUserTotalPins(sessions)));
                totalGames.setText("Games......................." + String.valueOf(sessions.size() * 3));
                totalSessions.setText("Sessions...................." + String.valueOf(sessions.size()));
            }
        });
    }

    public void setUserTitle() {
        DocumentReference docRef = db.collection("usersDB").document(currentUser.getEmail());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                getSupportActionBar().setTitle(user.getUserName() + "'s Stats");
            }
        });
    }

    public int getUserPerGameAverage(ArrayList<Session> sessions) {
        if (sessions.size() == 0) {
            return 0;
        }
        int total = 0;
        int overallAverage;
        for (int i = 0; i < sessions.size(); i++) {
            total += sessions.get(i).getTotalPins();
        }

        overallAverage = total / (sessions.size() * 3);
        return overallAverage;
    }

    public int getUserSessionAverage(ArrayList<Session> sessions) {
        if (sessions.size() == 0) {
            return 0;
        }
        int total = 0;
        int overallAverage;
        for (int i = 0; i < sessions.size(); i++) {
            total += sessions.get(i).getTotalPins();
        }

        overallAverage = total / sessions.size();
        return overallAverage;
    }

    public int getUserGameAverage(ArrayList<Session> sessions, int gameNum) {
        if (sessions.size() == 0) {
            return 0;
        }
        int total = 0;
        int gameAverage;
        switch (gameNum) {
            case 1:
                for (int i = 0; i < sessions.size(); i++) {
                    total += sessions.get(i).getGame1();
                }
                break;
            case 2:
                for (int i = 0; i < sessions.size(); i++) {
                    total += sessions.get(i).getGame2();
                }
                break;
            case 3:
                for (int i = 0; i < sessions.size(); i++) {
                    total += sessions.get(i).getGame3();
                }
                break;
            default:
                total = 0;
        }

        gameAverage = total / sessions.size();
        return gameAverage;
    }

    public int getUserTotalPins(ArrayList<Session> sessions) {
        if (sessions.size() == 0) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < sessions.size(); i++) {
            total += sessions.get(i).getTotalPins();
        }
        return total;
    }
}