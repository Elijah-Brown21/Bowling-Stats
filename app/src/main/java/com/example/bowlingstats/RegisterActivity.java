package com.example.bowlingstats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.TypefaceCompatUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class RegisterActivity extends Activity {

    private FirebaseAuth mAuth;
    private Button signUpBtn;
    private Button goToLoginBtn;
    private EditText emailEdit;
    private EditText pwEdit;
    private EditText nameEdit;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signUpBtn = findViewById(R.id.sign_up_btn);
        goToLoginBtn = findViewById(R.id.goToLogin_btn);
        emailEdit = findViewById(R.id.user_email_edit);
        pwEdit = findViewById(R.id.user_pw_edit);
        nameEdit = findViewById(R.id.user_name_edit);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), UpdatedStatsActivity.class));
            finish();
        }

        goToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                String pw = pwEdit.getText().toString();
                String userName = nameEdit.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    emailEdit.setError("Email is Required");
                    return;
                }

                if (TextUtils.isEmpty(pw)) {
                    pwEdit.setError("Password is Required");
                    return;
                }

                if (TextUtils.isEmpty(userName)) {
                    nameEdit.setError("Username is Required");
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), UpdatedStatsActivity.class));

                            String tempStrDate = "12/12/2012";
                            Date tempDate;
                            try {
                                tempDate = new SimpleDateFormat("MM/dd/yyyy").parse(tempStrDate);
                            } catch (ParseException e) {
                                tempDate = new GregorianCalendar(0,0,0).getTime();
                                e.printStackTrace();
                            }
                            Session session = new Session("Delete after first entry", tempDate, 0, 0, 0);
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            db.collection(currentUser.getEmail()).add(session);

                            User user = new User(userName, email);
                            db.collection("usersDB").document(email).set(user);
                            //finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}