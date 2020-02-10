package com.example.taptimingkeyboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseRegisterActivity extends AppCompatActivity {

    private static final String TAG = FirebaseRegisterActivity.class.getName();

    private FirebaseAuth mAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_register);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextPasswordConfirm = findViewById(R.id.edit_text_password_confirm);
        mAuth = FirebaseAuth.getInstance();
    }

    public void registerClick(View v) {
        if (editTextPassword.getText().length()<1 || editTextEmail.getText().length()<1) {
            Toast.makeText(FirebaseRegisterActivity.this, getString(R.string.text_empty_fields),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!editTextPassword.getText().toString().equals(editTextPasswordConfirm.getText().toString())) {
            Toast.makeText(FirebaseRegisterActivity.this, getString(R.string.text_passwords_differ),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString()).matches()) {
            Toast.makeText(FirebaseRegisterActivity.this, getString(R.string.text_wrong_email_addres),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final Button button = (Button)v;
        button.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "account created");
                            Toast.makeText(FirebaseRegisterActivity.this, getString(R.string.text_registration_succeed),
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Log.w(TAG, "account registration failed", task.getException());
                            Toast.makeText(FirebaseRegisterActivity.this, getString(R.string.text_registration_failed),
                                    Toast.LENGTH_SHORT).show();
                            button.setEnabled(true);
                        }
                    }
                });
    }
}