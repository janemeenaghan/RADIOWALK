package com.example.janecapstoneproject;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.SignUpCallback;

import org.json.JSONArray;

import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    public static final String TAG = "SignUpActivity";
    private EditText etEmail, etUsername,etPassword,etConfirmPassword;
    TextInputLayout etEmailWrapper,etUsernameWrapper,etPasswordWrapper,etConfirmPasswordWrapper;
    int returnValueEmail,returnValueUsername;
    String email,username;
    boolean validUsername,validEmail,matchingPasswords;
    boolean halted;
    private Button signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initViews();
        validUsername = false;
        validEmail = false;
        matchingPasswords = false;
        halted = false;
        initListeners();
    }
    private void initViews(){
        etEmail = findViewById(R.id.email);
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.passwordConfirm);
        etEmailWrapper = findViewById(R.id.tilWrapper1);
        etUsernameWrapper = findViewById(R.id.tilWrapper2);
        etPasswordWrapper = findViewById(R.id.tilWrapper3);
        etConfirmPasswordWrapper = findViewById(R.id.tilWrapper4);
    }
    private void initListeners(){
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
                    matchingPasswords = true;
                    etPasswordWrapper.setErrorEnabled(false);
                    etConfirmPasswordWrapper.setErrorEnabled(false);
                }
                else{
                    matchingPasswords = false;
                    etPasswordWrapper.setError("Passwords do not match.");
                    etConfirmPasswordWrapper.setError("Passwords do not match.");
                }
            }
        });
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
                    matchingPasswords = true;
                    etPasswordWrapper.setErrorEnabled(false);
                    etConfirmPasswordWrapper.setErrorEnabled(false);
                }
                else{
                    matchingPasswords = false;
                    etPasswordWrapper.setError("Passwords do not match.");
                    etConfirmPasswordWrapper.setError("Passwords do not match.");
                }
            }
        });
        signup = findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: other conditions
                if (matchingPasswords && !halted) {
                    String email = etEmail.getText().toString();
                    String username = etUsername.getText().toString();
                    String password = etPassword.getText().toString();
                    proceedWithEmail(email,username,password);
                }
            }
        });
    }
    private void proceedWithEmail(String email,String username,String password) {
        halted = true;
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo("email",email);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with email portion of signup", e);
                    Toast.makeText(SignUpActivity.this, "EMAILkys"+e, Toast.LENGTH_LONG).show();
                    halted = false;
                }
                else if (users.isEmpty()){
                    proceedWithUsername(email,username,password);
                }
                else{
                    etEmailWrapper.setError("Email address already in use.");
                    halted = false;
                }
            }
        });
    }
    private void proceedWithUsername(String email, String username,String password){
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo("username",username);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with username portion of signup", e);
                    Toast.makeText(SignUpActivity.this, "USERNAMEkys"+e, Toast.LENGTH_LONG).show();
                    halted = false;
                }
                else if (users.isEmpty()){
                    attemptToRegisterUser(email, username, password);
                }
                else{
                    etUsernameWrapper.setError("Username already in use");
                    halted = false;
                }
            }
        });
    }


    /*public void queryUsersForParameterStatus(String key, String value) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo(key,value);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with signup", e);
                    Toast.makeText(SignUpActivity.this, "KYS"+e, Toast.LENGTH_LONG);
                }
                else if (objects.isEmpty()){
                    if (key.equals("username")) {
                        returnValueUsername = 0;
                    }
                    else{
                        returnValueEmail = 0;
                    }
                }
                else{
                    if (key.equals("username")) {
                        returnValueUsername = 1;
                    }
                    else{
                        returnValueEmail = 1;
                    }
                }
            }
        });
    }*/

    private void attemptToRegisterUser(String email, String username, String password) {
        ParseUser user = new ParseUser();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.put("sharedStations",new JSONArray());
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    if (e.getCode() == 202){
                        etUsernameWrapper.setError("Invalid username");
                    }
                    else if (e.getCode() == 203){
                        etEmailWrapper.setError("Invalid email address");
                    }
                    else{
                        Toast.makeText(SignUpActivity.this, "Unknown error. Please try again or reinstall.", Toast.LENGTH_LONG).show();
                    }
                    halted = false;
                } else {
                    attemptToLogInUser(username, password);
                }
            }
        });
    }
    private void attemptToLogInUser(String username, String password){
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                halted = false;
                if (e != null) {
                    Toast.makeText(SignUpActivity.this, "Unknown error. Please try again or reinstall.", Toast.LENGTH_LONG).show();
                } else {
                    goMainActivity();
                }
            }
        });
    }
    private void goMainActivity() {
        Intent i = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}