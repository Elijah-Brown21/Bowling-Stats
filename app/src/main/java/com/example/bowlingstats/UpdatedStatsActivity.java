package com.example.bowlingstats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class UpdatedStatsActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    //Database components
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference sessionRef = db.collection(currentUser.getEmail());
    private ArrayList<Session> sessions = new ArrayList<>();

    //TODO: XML components
    LineChart statsChart;
    ImageButton listBtn, filterBtn, dateBtn;
    FirebaseAuth mAuth;

    //Backend Chart/Data components
    Long selectedEndDate;
    Long selectedStartDate;
    int optionSelected;
    ArrayList<Session> currentList = new ArrayList<>();

    TextView overallAvg;
    TextView totalSessions;
    TextView chartNum;
    TextView chartNumLabel;
    TextView chartSessions;
    TextView chartSessionsLabel;
    TextView chartDateLabel;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserTitle();
        setContentView(R.layout.updated_stats_page);

        mAuth = FirebaseAuth.getInstance();

        //TODO: Instantiate new elements
        statsChart = findViewById(R.id.stats_chart);
        overallAvg = findViewById(R.id.main_avg_number);
        totalSessions = findViewById(R.id.total_sessions_number);
        chartNum = findViewById(R.id.shown_stat_number);
        chartNumLabel = findViewById(R.id.shown_stat_label);
        chartSessions = findViewById(R.id.shown_sessions_number);
        chartSessionsLabel = findViewById(R.id.shown_sessions_label);
        chartDateLabel = findViewById(R.id.chart_info_date_label);

        //TODO: Should work now with the new icon list button
        listBtn = findViewById(R.id.list_btn);

        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UpdatedStatsActivity.this, MainActivity.class));
//                sessions.clear();
            }
        });

        filterBtn = findViewById(R.id.filter_btn);

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu filterMenu = new PopupMenu(UpdatedStatsActivity.this, v);
                filterMenu.setOnMenuItemClickListener(UpdatedStatsActivity.this);
                filterMenu.inflate(R.menu.filter_menu);
                filterMenu.show();
            }
        });

        dateBtn = findViewById(R.id.date_btn);
        MaterialDatePicker datePicker = MaterialDatePicker.Builder.dateRangePicker().build();

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show(getSupportFragmentManager(), "Tag_picker");

                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) { //Object class is a Pair
                        //Updates the global start and end dates
                        Pair dates = (Pair) selection;
                        selectedStartDate = (Long) dates.first;
                        selectedEndDate = (Long) dates.second;
                        System.out.println("From the picker " + selectedStartDate);
                        System.out.println("From the picker " + selectedEndDate);
                        updateChart();
                    }
                });
            }
        });
    }

    //TODO: Here are the different menu choices for the filter
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.average:
                optionSelected = R.id.average;
                updateChart();
                chartNumLabel.setText("Overall\nAverage");
                chartNum.setText(String.valueOf(getUserPerGameAverage(currentList)));
                break;
            case R.id.high_game:
                optionSelected = R.id.high_game;
                updateChart();
                chartNumLabel.setText("High Game\nAverage");
                chartNum.setText(String.valueOf(getUserGameAverage(currentList, R.id.high_game)));
                break;
            case R.id.game_1:
                optionSelected = R.id.game_1;
                updateChart();
                chartNumLabel.setText("Game 1\nAverage");
                chartNum.setText(String.valueOf(getUserGameAverage(currentList, R.id.game_1)));
                break;
            case R.id.game_2:
                optionSelected = R.id.game_2;
                updateChart();
                chartNumLabel.setText("Game 2\nAverage");
                chartNum.setText(String.valueOf(getUserGameAverage(currentList, R.id.game_2)));
                break;
            case R.id.game_3: 
                optionSelected = R.id.game_3;
                updateChart();
                chartNumLabel.setText("Game 3\nAverage");
                chartNum.setText(String.valueOf(getUserGameAverage(currentList, R.id.game_3)));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return false;
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
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(UpdatedStatsActivity.this);
                confirmDialog.setTitle("Logout?");
                confirmDialog.setMessage("You will be required to enter your email and password for re-entry.");
                confirmDialog.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        startActivity(new Intent(UpdatedStatsActivity.this, LoginActivity.class));
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
        statsChart.invalidate();
        sessions.clear();
        Query query = sessionRef.orderBy("timestamp", Query.Direction.DESCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(UpdatedStatsActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }


                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Session session = documentSnapshot.toObject(Session.class);
                    sessions.add(session);
                }

                //TODO: This is where stuff was actually shown
                overallAvg.setText(String.valueOf(getUserPerGameAverage(sessions)));
                totalSessions.setText(String.valueOf(sessions.size()));

                //In the old version, the stats were updated once the page refreshed, so the functions
                //will have to change so that the filter/date is changing what is shown
                //Thus, on start, these functions should probably be called with some default parameters

                styleChart();
                setStatsChart();
            }
        });
    }

    //TODO: This should be the controller for the chart, and the date/filter functions will interact with it
    public void setStatsChart() {
        List<Entry> entries = new ArrayList<>();
        currentList.clear();

        for (int i = 0; i < 10; i++) {
            if (i < sessions.size()) {
                Session current = sessions.get(i);
                entries.add(new Entry(current.getTimestamp().getTime(), current.getAverage()));
                currentList.add(current);
            }
        }

        Collections.sort(entries, new EntryXComparator());

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setDrawValues(false);

        statsChart.setData(new LineData(dataSet));
        statsChart.animateX(1000);
        statsChart.invalidate();
        chartSessions.setText(String.valueOf(entries.size()));

        System.out.println("Should be start date " + entries.get(0).getX() + " for the number " + entries.get(0).getY());
        System.out.println("Should be end date " + entries.get(entries.size()-1).getX() + " for the number " + entries.get(entries.size()-1).getY());

        //Configure current selected dates and chart label based on start and end dates of the past 10 sessions
        DateFormat df = new SimpleDateFormat("MM/dd/yy", Locale.US);
        selectedStartDate = sessions.get(currentList.size()-1).getTimestamp().getTime() - 28800000;
        selectedEndDate = sessions.get(0).getTimestamp().getTime() - 28800000;

        chartDateLabel.setText(df.format(selectedStartDate + 28800000) + " - " + df.format(selectedEndDate + 28800000));
        chartNumLabel.setText("Overall\nAverage");
        chartNum.setText(String.valueOf(getUserPerGameAverage(currentList)));
    }

    public void updateChart() {
        List<Entry> entries = new ArrayList<>();

        currentList.clear();

        for (Session session : sessions) {
            if (session.getTimestamp().getTime() - 28800000 >= selectedStartDate && session.getTimestamp().getTime() - 28800000 <= selectedEndDate) {
                entries.add(new Entry(session.getTimestamp().getTime(), optionGetter(session)));
                currentList.add(session);
            }
        }

        Collections.sort(entries, new EntryXComparator());

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setDrawValues(false);

        statsChart.setData(new LineData(dataSet));
        statsChart.animateX(1000);
        statsChart.invalidate();
        chartSessions.setText(String.valueOf(entries.size()));

        DateFormat df = new SimpleDateFormat("MM/dd/yy", Locale.US);
        chartDateLabel.setText(df.format(selectedStartDate + 28800000) + " - " + df.format(selectedEndDate + 28800000));
        //TODO: Update chart num label and number based on filter selection
    }

    public void styleChart() {
        statsChart.getDescription().setEnabled(false);
        statsChart.setBackgroundColor(Color.BLACK);
        statsChart.setNoDataTextColor(Color.YELLOW);
        statsChart.setNoDataText("There are no sessions in the selected date range");
        statsChart.setDrawMarkers(false);
        statsChart.getLegend().setEnabled(false);
        statsChart.setGridBackgroundColor(Color.WHITE);
        statsChart.getXAxis().setDrawAxisLine(false);
        statsChart.getXAxis().setDrawGridLines(false);
        statsChart.getXAxis().setDrawLabels(false);
        statsChart.getAxisLeft().setDrawAxisLine(false);
        statsChart.getAxisLeft().setTextColor(Color.WHITE);
        statsChart.getAxisLeft().setTextSize(12);
        statsChart.getAxisRight().setMaxWidth(1);
        statsChart.getAxisRight().setDrawAxisLine(false);
        statsChart.getAxisRight().setDrawGridLines(false);
    }



    //TODO: Keep same
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

    //TODO: Should all be the same, as they are just computation tools
    public int getUserPerGameAverage(ArrayList<Session> sessionList) {
        if (sessionList.size() == 0) {
            return 0;
        }
        int total = 0;
        int overallAverage;
        for (int i = 0; i < sessionList.size(); i++) {
            total += sessionList.get(i).getTotalPins();
        }

        overallAverage = total / (sessionList.size() * 3);
        return overallAverage;
    }

    public int getUserSessionAverage(ArrayList<Session> sessionList) {
        if (sessionList.size() == 0) {
            return 0;
        }
        int total = 0;
        int overallAverage;
        for (int i = 0; i < sessionList.size(); i++) {
            total += sessionList.get(i).getTotalPins();
        }

        overallAverage = total / sessionList.size();
        return overallAverage;
    }

    public int getUserGameAverage(ArrayList<Session> sessionList, int gameNum) {
        if (sessionList.size() == 0) {
            return 0;
        }
        int total = 0;
        int gameAverage;
        switch (gameNum) {
            case R.id.game_1:
                for (int i = 0; i < sessionList.size(); i++) {
                    total += sessionList.get(i).getGame1();
                }
                break;
            case R.id.game_2:
                for (int i = 0; i < sessionList.size(); i++) {
                    total += sessionList.get(i).getGame2();
                }
                break;
            case R.id.game_3:
                for (int i = 0; i < sessionList.size(); i++) {
                    total += sessionList.get(i).getGame3();
                }
                break;
            case R.id.high_game:
                for (int i = 0; i < sessionList.size(); i++) {
                    total += sessionList.get(i).getHighGame();
                }
                break;
            default:
                total = 0;
        }

        gameAverage = total / sessionList.size();
        return gameAverage;
    }

    public int getUserTotalPins(ArrayList<Session> sessionList) {
        if (sessionList.size() == 0) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < sessionList.size(); i++) {
            total += sessionList.get(i).getTotalPins();
        }
        return total;
    }
    
    private int optionGetter(Session current) {
        int attr;
        switch (optionSelected) {
            case R.id.average:
                attr = current.getAverage();
                break;
            case R.id.high_game:
                attr = current.getHighGame();
                break;
            case R.id.game_1:
                attr = current.getGame1();
                break;
            case R.id.game_2:
                attr = current.getGame2();
                break;
            case R.id.game_3:
                attr = current.getGame3();
                break;
            default:
                attr = current.getAverage();
        }
        return attr;
    }

}