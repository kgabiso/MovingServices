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
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    TextView CPasswordfield ,emailField,passwordFild;
    Button btn_Signup;
    private SpotsDialog dialog;
    private FirebaseAuth mAthu;
    private DatabaseReference mDatabase;
    private LovelyStandardDialog lovelyStandardDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dialog = new SpotsDialog(this);
        mAthu = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        CPasswordfield =(TextView)findViewById(R.id.reg_cpassword);
        emailField =(TextView)findViewById(R.id.reg_email);
        passwordFild =(TextView)findViewById(R.id.reg_password);
        btn_Signup =(Button)findViewById(R.id.btn_register);
        lovelyStandardDialog = new LovelyStandardDialog(this);
        btn_Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegistration();
            }
        });
    }

    private void startRegistration() {
        final String CPassword = CPasswordfield.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordFild.getText().toString().trim();

        if(!TextUtils.isEmpty(CPassword) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            if(CPassword.equals(password)) {
                dialog.show();
                mAthu.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                            dialog.dismiss();

                        } else {
                            lovelyStandardDialog
                                    .setTopColorRes(R.color.colorAccent)
                                    .setButtonsColorRes(R.color.colorPrimaryDark)
                                    .setIcon(R.drawable.delivery_truck_icon)
                                    .setTitle("Sign up Unsuccessful")
                                    .setMessage(task.getException().getMessage())
                                    .setNeutralButton(android.R.string.ok, null)
                                    .show();
                        }
                    }
                });
            }else {
                Toast.makeText(getApplicationContext(),"Password and the confirmation password must be the same",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),"Please enter email and password to Sign up",Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    }
}
