package com.ganesh.travelapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.ganesh.travelapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.User;

public class MainLoginActivity extends AppCompatActivity {

    private static final String TAG = "MainLoginActivity";
    LinearLayout l;
    Button login, signup, fb_login_button;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;
    FirebaseUser user;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity);

        l = (LinearLayout) findViewById(R.id.l);
        login = (Button) findViewById(R.id.login);
        signup = (Button) findViewById(R.id.signup);
        fb_login_button = (Button) findViewById(R.id.fb_login_button);

        TextView skip = (TextView) findViewById(R.id.skip);

        Animation bottom_slide = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        l.startAnimation(bottom_slide);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    startIntent();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startIntent();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainLoginActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainLoginActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

        fb_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoginClick(view);
            }
        });
    }

    public void startIntent() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void onLoginClick(View v) {
        final ProgressDialog d = new ProgressDialog(this);
        d.setMessage("Verifying Credentials...");
        d.setIndeterminate(true);
        d.setCanceledOnTouchOutside(false);
        d.show();

        final List<String> permissions = new ArrayList<String>();
        permissions.add("public_profile");
        permissions.add("email");

        mCallbackManager = CallbackManager.Factory.create();
        v.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                d.dismiss();
                if (loginResult == null) {
                    if (user == null) {
                        FirebaseAuth.getInstance().signOut();
                    } else {
                        getFbData();
                    }
                } else {
                    Log.d("error1", e.getMessage());

                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //getting facebook data.......................................................................
    public void getFbData() {

        final ProgressDialog d = new ProgressDialog(this);
        d.setIndeterminate(true);
        d.setMessage("Verifying Credentials....");
        d.setCanceledOnTouchOutside(false);
        d.show();

        //<------------------------------------------getting gender , name , email ---------------------------------->
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {

                        Log.d("responseFacebook", graphResponse.toString());
                        try {
                            final String id, email, name;

                            email = jsonObject.getString("email");
                            name = jsonObject.getString("name");

                            //Save user info and username
                            writeNewUser(userId,name,email);
                        } catch (JSONException e) {
                            d.dismiss();
                        }
                    }
                });

        Bundle params = new Bundle();
        params.putString("fields", "id,name,email");
        request.setParameters(params);
        request.executeAsync();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(FacebookLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @IgnoreExtraProperties
    public class User {

        public String username;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }

    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);
    }

}
