package com.example.bowlingstats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class SessionRecViewAdapter extends FirestoreRecyclerAdapter<Session, SessionRecViewAdapter.ViewHolder> {
    private OnItemClickListener listener;

    public SessionRecViewAdapter(FirestoreRecyclerOptions<Session> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(SessionRecViewAdapter.ViewHolder holder, int position, Session model) {
        holder.date.setText("Date: " + model.getDate());
        holder.average.setText("Average: " + String.valueOf(model.getAverage()));
        holder.totalPins.setText("Total Pins: " + String.valueOf(model.getTotalPins()));
        holder.highGame.setText("High Game: " + String.valueOf(model.getHighGame()));
        //TODO: Set the actions for each instance using = holder.<ui element>.<action>(model.<action in class>)
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_list_item, parent, false);
        return new ViewHolder(v);
    }

    public void deleteSession(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView parent;
        TextView date;
        TextView average;
        TextView totalPins;
        TextView highGame;
        //TODO: Add in the UI elements for the view
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            date = itemView.findViewById(R.id.dateView);
            average = itemView.findViewById(R.id.averageView);
            totalPins = itemView.findViewById(R.id.totalView);
            highGame = itemView.findViewById(R.id.highView);
            //TODO: Initialize UI elements by itemView.fb(UI element name)

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
