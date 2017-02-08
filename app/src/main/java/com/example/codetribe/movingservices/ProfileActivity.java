package com.example.codetribe.movingservices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class ProfileActivity extends AppCompatActivity {

    private TextView displayname,email,sign_out;
    private RelativeLayout prof_pic_upload;
    private CircleImageView prof_pic;
    private DatabaseReference mDB_users;
    private FirebaseAuth mAuth;
    private Button btn_Update;
    private EditText ed_displayName,ed_lastName;
    SpotsDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dialog = new SpotsDialog(this);
        mDB_users = FirebaseDatabase.getInstance().getReference().child("users");
        mDB_users.keepSynced(true);
        mAuth =FirebaseAuth.getInstance();
        displayname =(TextView)findViewById(R.id.txtprofile_display_name);
        email =(TextView)findViewById(R.id.txt_profile_email);
        sign_out =(TextView)findViewById(R.id.txt_sign_out);
        prof_pic_upload =(RelativeLayout)findViewById(R.id.profile_photo);
        prof_pic =(CircleImageView)findViewById(R.id.circle_profile_image);
        btn_Update =(Button)findViewById(R.id.btn_update);
        ed_displayName =(EditText)findViewById(R.id.ed_displayName);
        ed_lastName =(EditText)findViewById(R.id.ed_lastName);
        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_profile();
            }
        });
        prof_pic_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        if(mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            mDB_users.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String uri_profile = dataSnapshot.child("profileimage").getValue().toString();
                    String name = (String)dataSnapshot.child("name").getValue();
                    String lname = (String)dataSnapshot.child("lname").getValue();
                    Glide.with(ProfileActivity.this).load(uri_profile).centerCrop().into(prof_pic);
                    displayname.setText(name +" "+lname);
                    ed_displayName.setHint(name);
                    email.setText(mAuth.getCurrentUser().getEmail());
                    System.out.println(dataSnapshot.child("profileimage").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    public void update_profile(){
        if(mAuth.getCurrentUser() != null) {
            dialog.show();
            final String uid = mAuth.getCurrentUser().getUid();
            mDB_users.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!TextUtils.isEmpty(ed_displayName.getText().toString().trim())) {
                        mDB_users.child(uid).child("name").setValue(ed_displayName.getText().toString().trim());
                    }
                    if(!TextUtils.isEmpty(ed_lastName.getText().toString().trim()))
                    {
                        mDB_users.child(uid).child("lname").setValue(ed_lastName.getText().toString().trim());

                    }
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                }
            });
        }


    }
}
