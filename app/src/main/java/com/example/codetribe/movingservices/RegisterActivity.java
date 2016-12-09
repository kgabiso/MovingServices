package com.example.codetribe.movingservices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    TextView namefield ,emailField,passwordFild;
    Button btn_Signup;
    private SpotsDialog dialog;
    private FirebaseAuth mAthu;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dialog = new SpotsDialog(this);
        mAthu = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        namefield =(TextView)findViewById(R.id.reg_name);
        emailField =(TextView)findViewById(R.id.reg_email);
        passwordFild =(TextView)findViewById(R.id.reg_password);
        btn_Signup =(Button)findViewById(R.id.btn_register);

        btn_Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegistration();
            }
        });
    }

    private void startRegistration() {
        final String name = namefield.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordFild.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            dialog.show();

            mAthu.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                       // String user_id = mAthu.getCurrentUser().getUid();
                        //DatabaseReference current_user_id = mDatabase.child(user_id);
                        //current_user_id.child("name").setValue(name);
                        //current_user_id.child("image").setValue("defult");

                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                        dialog.dismiss();

                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(),"Please enter email and password to Sign up",Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }
}
