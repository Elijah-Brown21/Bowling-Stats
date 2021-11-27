package com.example.bowlingstats;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Session {
    private String date;
    private @ServerTimestamp Date timestamp;
    private int game1;
    private int game2;
    private int game3;
    private int average;
    private int totalPins;

    public Session() {}

    public Session(String date, Date timestamp, int game1, int game2, int game3) {
        this.date = date;
        this.timestamp = timestamp;
        this.game1 = game1;
        this.game2 = game2;
        this.game3 = game3;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String sDate) {
        this.date = sDate;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getGame1() {
        return game1;
    }

    public void setGame1(int game1) {
        this.game1 = game1;
    }

    public int getGame2() {
        return game2;
    }

    public void setGame2(int game2) {
        this.game2 = game2;
    }

    public int getGame3() {
        return game3;
    }

    public void setGame3(int game3) {
        this.game3 = game3;
    }

    public int getAverage() {
        this.average = (this.game1 + this.game2 + this.game3) / 3;
        return this.average;
    }

    public int getTotalPins() {
        this.totalPins = this.game1 + this.game2 + this.game3;
        return this.totalPins;
    }

    public int getHighGame() {
        int[] games = {this.game1, this.game2, this.game3};
        int max = games[0];
        for (int i = 1; i < games.length; i++) {
            if (max < games[i]) {
                max = games[i];
            }
        }
        return max;
    }
}
