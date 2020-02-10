package com.example.taptimingkeyboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.taptimingkeyboard.activity.UserInfoActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FireBaseLoginActivity extends AppCompatActivity {

    private static final String TAG = FireBaseLoginActivity.class.getName();

    private FirebaseAuth mAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_base_login);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null) {
            loggedIn(currentUser);
        }

    }

    public void loginClick(View v) {
        final Button button = (Button)v;
        button.setEnabled(false);
        mAuth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "firebase email login succeed");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loggedIn(user);
                        } else {
                            Log.w(TAG, "firebase email login failed", task.getException());
                            button.setEnabled(true);
                            Toast.makeText(FireBaseLoginActivity.this, getString(R.string.login_text_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loggedIn(FirebaseUser firebaseUser) {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("started_from_preferences", false);
        startActivity(intent);
        finish();
    }

}
