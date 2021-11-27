package com.example.bowlingstats;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class GameDialog extends Dialog {

    public Activity act;
    public Dialog dial;
    public Session session;

    public TextView dateDialView, game1DialView, game2DialView, game3DialView;

    public GameDialog(Activity a, Session s) {
        super(a);
        this.act = a;
        this.session = s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.game_dialog);

        dateDialView = findViewById(R.id.date_dialog);
        game1DialView = findViewById(R.id.game1_dialog);
        game2DialView = findViewById(R.id.game2_dialog);
        game3DialView = findViewById(R.id.game3_dialog);

        dateDialView.setText(session.getDate());
        game1DialView.setText("Game 1: " + String.valueOf(session.getGame1()));
        game2DialView.setText("Game 2: " + String.valueOf(session.getGame2()));
        game3DialView.setText("Game 3: " + String.valueOf(session.getGame3()));
    }
}
