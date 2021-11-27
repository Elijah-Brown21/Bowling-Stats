package com.example.bowlingstats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.CalendarView;

import com.google.firebase.firestore.FieldValue;

public class CalendarActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_activity")) {
                finish();
            }
        }
    };

    CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.view_calendar);

        registerReceiver(broadcastReceiver, new IntentFilter("finish_activity"));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                Intent openSessionActivity = new Intent(CalendarActivity.this, NewSessionActivity.class);
                openSessionActivity.putExtra("date", date);
                startActivity(openSessionActivity);
            }
        });
    }
}