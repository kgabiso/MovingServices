package com.example.codetribe.movingservices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import dmax.dialog.SpotsDialog;

public class reset_passwordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText reset_email;
    private Button btn_reset;
    private LovelyStandardDialog lovelyStandardDialog;
    private SpotsDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        lovelyStandardDialog = new LovelyStandardDialog(this);
        mAuth = FirebaseAuth.getInstance();

        reset_email = (EditText) findViewById(R.id.reset_email);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        dialog = new SpotsDialog(this);


        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = reset_email.getText().toString().trim();
                if (!TextUtils.isEmpty(email)) {

                    dialog.show();
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                lovelyStandardDialog
                                        .setTopColorRes(R.color.colorAccent)
                                        .setButtonsColorRes(R.color.colorPrimaryDark)
                                        .setIcon(R.drawable.delivery_truck_icon)
                                        .setTitle("Reset Password Successful")
                                        .setMessage("We have sent you instructions to reset your password!")
                                        .setNeutralButton(android.R.string.ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent passw_Intent = new Intent(reset_passwordActivity.this, LoginActivity.class);
                                                startActivity(passw_Intent);
                                                finish();
                                            }
                                        })
                                        .show();

                            }else {
                                lovelyStandardDialog
                                        .setTopColorRes(R.color.colorAccent)
                                        .setButtonsColorRes(R.color.colorPrimaryDark)
                                        .setIcon(R.drawable.delivery_truck_icon)
                                        .setTitle("Reset Password Unsuccessful")
                                        .setMessage(task.getException().getMessage())
                                        .setNeutralButton(android.R.string.ok, null)
                                        .show();
                            }

                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }
}
