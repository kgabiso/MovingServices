package com.example.codetribe.movingservices;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView log_email, log_password;
    private Button btn_log;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUser;
    private SpotsDialog dialog;
    private SignInButton googleButton;
    private final static int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "LoginActivity";
    private FirebaseAuth.AuthStateListener mAuthListner;
    private LovelyStandardDialog lovelyStandardDialog;

    private TextView signUp , forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dialog = new SpotsDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUser.keepSynced(true);

        lovelyStandardDialog = new LovelyStandardDialog(this);
        googleButton = (SignInButton) findViewById(R.id.btn_sign_in);
        btn_log = (Button) findViewById(R.id.btn_login);
        log_email = (EditText) findViewById(R.id.log_email);
        log_password = (EditText) findViewById(R.id.log_password);
        signUp = (TextView) findViewById(R.id.log_sign_up);
        forgot_password =(TextView)findViewById(R.id.log_forgot_password);
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent passw_Intent = new Intent(LoginActivity.this, reset_passwordActivity.class);
                startActivity(passw_Intent);
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUp_Intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(signUp_Intent);
            }
        });


        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        btn_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login();
            }
        });


// Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), connectionResult.toString(), Toast.LENGTH_SHORT).show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        dialog.show();
    }

    private void Login() {
        String email = log_email.getText().toString().trim();
        String password = log_password.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            dialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        checkUserExist();
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        lovelyStandardDialog
                                .setTopColorRes(R.color.colorAccent)
                                .setButtonsColorRes(R.color.colorPrimaryDark)
                                .setIcon(R.drawable.delivery_truck_icon)
                                .setTitle("Login Unsuccessful")
                                .setMessage(task.getException().getMessage())
                                .setNeutralButton(android.R.string.ok, null)
                                .show();
                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(),"Please enter email and password to login",Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            dialog.show();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...

                dialog.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
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
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            checkUserExist();
                        }
                        // ...

                    }
                });
    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user_id)) {

                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        Intent SetupIntent = new Intent(LoginActivity.this, SetUpActivity.class);
                        SetupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(SetupIntent);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No user", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            dialog.show();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    dialog.dismiss();
                    handleSignInResult(googleSignInResult);
                }
            });
        }

    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            mAuth.removeAuthStateListener(mAuthListner);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }
}
