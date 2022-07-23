package com.example.janecapstoneproject;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.facebook.login.LoginManager;
import com.parse.ParseUser;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;
import org.json.JSONException;
import java.util.Arrays;
import java.util.Collection;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.facebook.ParseFacebookUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.facebook.login.LoginManager;
import com.parse.ParseUser;
import java.util.Collection;

import java.util.Arrays;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button login;
    private TextView createAccount;
    private Button fbLoginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        login = findViewById(R.id.signup);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });
        createAccount = findViewById(R.id.createAccountButton);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSignUpActivity();
            }
        });
        initFBLogin();
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }
    }
    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback(){
            @Override
            public void done(ParseUser user, ParseException e){
                if (e != null){
                    Toast.makeText(LoginActivity.this, "Incorrect username or password!", Toast.LENGTH_SHORT);
                    return;
                }
                goMainActivity();
                Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT);
            }
        });
    }
    private void goMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    private void goSignUpActivity() {
        Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(i);
        finish();
    }
    private void initFBLogin() {
        fbLoginButton = findViewById(R.id.fbLoginButton);
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<String> permissions = Arrays.asList("public_profile","email");
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e!=null){
                            ParseUser.logOut();
                        }if (user==null){
                            ParseUser.logOut();
                            Toast.makeText(LoginActivity.this, "The user cancelled the Facebook Login", Toast.LENGTH_SHORT).show();
                        }else if (user.isNew()){
                            Toast.makeText(LoginActivity.this, "User signed up and Logged in through Facebook", Toast.LENGTH_SHORT).show();
                            getUserDetailFromFB();
                        }else {
                            Toast.makeText(LoginActivity.this, "User logged in through Facebook", Toast.LENGTH_SHORT).show();
                            getUserDetailFromParse();
                        }
                    }
                });
            }
        });
    }
    private void getUserDetailFromParse() {
        ParseUser user = ParseUser.getCurrentUser();
        String title = "Welcome Back";
        String message = "User: " + user.getUsername() + "\n" + "Login Email: " + user.getEmail();
        alertDisplayer(title,message);
    }
    private void alertDisplayer(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }
    private void getUserDetailFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                ParseUser user = ParseUser.getCurrentUser();
                try {
                    user.setUsername(object.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    user.setEmail(object.getString("email"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        alertDisplayer("First time Login","Welcome!");
                    }
                });
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode,resultCode,data);
    }
}