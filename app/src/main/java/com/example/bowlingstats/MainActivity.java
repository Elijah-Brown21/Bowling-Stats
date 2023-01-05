package com.example.bowlingstats;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    // Firestore Recycler variables
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference sessionRef = db.collection(currentUser.getEmail());
    private SessionRecViewAdapter adapter;
    FloatingActionButton fabAdd;
    FloatingActionButton fabBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUserTitle();
        setUpRecyclerView(); //TODO: Main set up for recycler view

        fabAdd = findViewById(R.id.add_button);
        fabAdd.setOnClickListener(new View.OnClickListener() { // Creates Activity to get user input
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CalendarActivity.class));
            }
        });

        fabBack = findViewById(R.id.back_button);
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //TODO: Possible source of error for update, may need to start instead of return
                Intent openStatsActivity = new Intent(MainActivity.this, UpdatedStatsActivity.class);
                openStatsActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityIfNeeded(openStatsActivity, 0);
                finish();
            }
        });
    }

    private void setUpRecyclerView() {
        Query query = sessionRef.orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Session> options = new FirestoreRecyclerOptions.Builder<Session>()
                .setQuery(query, Session.class)
                .build();

        adapter = new SessionRecViewAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.sessionRecView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(MainActivity.this);
                confirmDialog.setTitle("Delete Session?");
                confirmDialog.setMessage("This session will be deleted from your storage and cannot be recovered.");
                confirmDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.deleteSession(viewHolder.getAdapterPosition());
                        Toast.makeText(MainActivity.this, "Session Deleted", Toast.LENGTH_SHORT).show();
                    }
                });

                confirmDialog.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        Toast.makeText(MainActivity.this, "Session Retained", Toast.LENGTH_SHORT).show();
                    }
                });
                confirmDialog.show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new SessionRecViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Session session = documentSnapshot.toObject(Session.class);
                GameDialog gd = new GameDialog(MainActivity.this, session);
                gd.show();
            }
        });
    }

    public void updateRecyclerView(String sortChoice) {
        Query newQuery = sessionRef.orderBy(sortChoice, Query.Direction.DESCENDING); //TODO: Removed limit, should be infinite now

        FirestoreRecyclerOptions<Session> newOptions = new FirestoreRecyclerOptions.Builder<Session>()
                .setQuery(newQuery, Session.class)
                .build();

        adapter.updateOptions(newOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sorting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemAverage:
                updateRecyclerView("average");
                return true;
            case R.id.itemHighGame:
                updateRecyclerView("highGame");
                return true;
            case R.id.itemTotalPins:
                updateRecyclerView("totalPins");
                return true;
            case R.id.itemDate:
                updateRecyclerView("timestamp");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUserTitle() {
        DocumentReference docRef = db.collection("usersDB").document(currentUser.getEmail());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                getSupportActionBar().setTitle(user.getUserName() + "'s Sessions");
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
        adapter.stopListening();
    }
}