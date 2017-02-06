package com.ganesh.travelapp.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ganesh.travelapp.R;
import com.ganesh.travelapp.app.MainApplication;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText usernameTxt, passwordTxt;
    String username, password;
    ImageView cross;
    Button login;
    TextView newuser;
    ProgressDialog progressDialog;
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;
    TextView changePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        passwordTxt = (EditText) findViewById(R.id.passwordTxt);
        usernameTxt = (EditText) findViewById(R.id.usernameTxt);
        login = (Button) findViewById(R.id.login);
        newuser = (TextView) findViewById(R.id.newuser);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToFireBase();
            }
        });

        newuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
                finish();*/
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Verifying Credentials...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void sendDataToFireBase() {
        if (passwordTxt.getText().length() != 0 && usernameTxt.getText().length() != 0) {

            progressDialog.show();

            username = usernameTxt.getText().toString();
            password = passwordTxt.getText().toString();
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (task.isSuccessful()) {
                                // If user exist and authenticated, send user to Welcome.class
                                Intent intent = new Intent(
                                        LoginActivity.this,
                                        MainActivity.class);
                                intent.putExtra("login_from", "custom");
                                Loginfrom("custom");
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Wrong username or password",
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    });


        } else {
            if (usernameTxt.getText().length() == 0) {
                usernameTxt.setError("Cannot be left blank");
            }
            if (passwordTxt.getText().length() == 0) {
                passwordTxt.setError("Cannot be left blank");
            }
        }
    }

    //method to store from where the user logged in..............
    public void Loginfrom(String mode) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("login_from", mode);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tracker t = ((MainApplication) getApplicationContext()).getTracker(MainApplication.TrackerName.APP_TRACKER);
        t.setScreenName("Login Screen");
        t.send(new HitBuilders.AppViewBuilder().build());
    }
}
