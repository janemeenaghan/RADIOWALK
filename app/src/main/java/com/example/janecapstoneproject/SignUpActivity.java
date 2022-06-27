package com.example.janecapstoneproject;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SignUpActivity extends AppCompatActivity {
    public static final String TAG = "SignUpActivity";
    private EditText etEmail, etUsername,etPassword,etConfirmPassword;
    private Button signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.email);
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.passwordConfirm);
        signup = findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Will add Counter text, requirements for passwords, checking that email exists, etc. at some point later
                String email = etEmail.getText().toString();
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                if (!etPassword.getText().equals(etConfirmPassword.getText())){
                    Toast.makeText(SignUpActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT);
                }
                //a bunch of other checks will come here at some point but it's most efficient to wait until I'm using the non-temporary database
                else{
                    registerUser(username, password);
                }
            }
        });
    }
    private void registerUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback(){
            @Override
            public void done(ParseUser user, ParseException e){
                if (e != null){
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(SignUpActivity.this, "Incorrect username or password!", Toast.LENGTH_SHORT);
                    return;
                }
                //Wondering if I should make a confirm email... personally I don't think it matters much but if it would be appreciated by Meta then I can try it
                goMainActivity();
                Toast.makeText(SignUpActivity.this, "Success!", Toast.LENGTH_SHORT);
            }
        });
    }
    private void goMainActivity() {
        Intent i = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}