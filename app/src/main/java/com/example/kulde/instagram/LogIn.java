package com.example.kulde.instagram;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.kulde.instagram.Home.MainPage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class LogIn extends AppCompatActivity  {

    // Variables
    private Button signin;
    private EditText email;
    private EditText password;
    private AppCompatCheckBox revealPass;
    private TextView signup;
    private ProgressDialog progressDialog;

    private static final String TAG = "Login Activity";

    private Context thisActivity;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        ConstraintLayout cl = findViewById(R.id.relativeLayout);
        AnimationDrawable draw = (AnimationDrawable) cl.getBackground();
        draw.setEnterFadeDuration(5000);
        draw.setExitFadeDuration(2000);
        draw.start();

        thisActivity = LogIn.this;

        progressDialog = new ProgressDialog(this);
        email = findViewById(R.id.emailid);
        password = findViewById(R.id.passwordet);
        revealPass = findViewById(R.id.cbRevealpass);

        setupFirebaseAuth();
        init();
    }



    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

    private void init(){
        Button bLogin = (Button) findViewById(R.id.btsignin);
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick() called with: v = [" + v + "]");
                String emailText = email.getText().toString().trim();
                String passwordText = password.getText().toString().trim();

                if(isStringNull(emailText) && isStringNull(passwordText)){
                    Toast.makeText(thisActivity, "Please Enter email and password", Toast.LENGTH_SHORT).show();
                } else{
                    progressDialog.setMessage("Login.....");
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(LogIn.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (!task.isSuccessful()) {
                                Log.d(TAG, "sign in with email failed");
                                Toast.makeText(thisActivity,"Aunthentication Failed",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                try{
                                    /*if(CHECK_IF_VERIFIED){*/

                                        Log.d(TAG, "onComplete: success. email is verified.");
                                        Intent intent = new Intent(thisActivity, MainPage.class);
                                        startActivity(intent);
                                        finish();


                                }catch (NullPointerException e){
                                    Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage() );
                                }
                            }
                        }
                    });
                }
            }
        });

        TextView linkSignUp = (TextView) findViewById(R.id.signuptext);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: for signup");
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });

        revealPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(thisActivity, MainPage.class);
            startActivity(intent);
            finish();
        }
    }

    //firebase thing starts here

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    //somebody signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in" + user.getUid());
                } else{
                    //nobodys here
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}