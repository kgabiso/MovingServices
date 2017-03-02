package com.example.codetribe.movingservices;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class ProfileActivity extends AppCompatActivity {

    private TextView displayname, email, sign_out, refresh;
    private RelativeLayout prof_pic_upload;
    private CircleImageView prof_pic;
    private DatabaseReference mDB_users;
    private FirebaseAuth mAuth;
    private Button btn_Update;
    private EditText ed_displayName, ed_lastName;
    SpotsDialog dialog;
    private static int Gallery_REQUEST = 1;
    private Uri imgUri;
    private StorageReference storage;
    private LovelyStandardDialog lovelyStandardDialog;
    private FirebaseAuth.AuthStateListener mAuthListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        lovelyStandardDialog = new LovelyStandardDialog(this);
        dialog = new SpotsDialog(this);
        mDB_users = FirebaseDatabase.getInstance().getReference().child("users");
        storage = FirebaseStorage.getInstance().getReference().child("profile_picture");
        mDB_users.keepSynced(true);
        mDB_users.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        displayname = (TextView) findViewById(R.id.txtprofile_display_name);
        email = (TextView) findViewById(R.id.txt_profile_email);
        sign_out = (TextView) findViewById(R.id.txt_sign_out);
        prof_pic_upload = (RelativeLayout) findViewById(R.id.profile_photo);
        prof_pic = (CircleImageView) findViewById(R.id.circle_profile_image);
        btn_Update = (Button) findViewById(R.id.btn_update);
        ed_displayName = (EditText) findViewById(R.id.ed_displayName);
        ed_lastName = (EditText) findViewById(R.id.ed_lastName);
        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_profile();
            }
        });
        refresh = (TextView) findViewById(R.id.txtRefresh);
        AuthListener();
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                fetchProfile();
            }
        });


        prof_pic_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_REQUEST);
            }
        });
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lovelyStandardDialog
                        .setTopColorRes(R.color.colorAccent)
                        .setButtonsColorRes(R.color.colorPrimaryDark)
                        .setIcon(R.drawable.delivery_truck_icon)
                        .setTitle("Sign out")
                        .setMessage("Do you want to Sign out ?")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mAuth.signOut();
                                AuthListener();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();


            }
        });
        fetchProfile();
    }

    public void fetchProfile() {
        if (mAuth.getCurrentUser() != null) {
            dialog.show();
            String uid = mAuth.getCurrentUser().getUid();
            mDB_users.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String uri_profile = dataSnapshot.child("profileimage").getValue().toString();
                    String name = (String) dataSnapshot.child("name").getValue();
                    String lname = (String) dataSnapshot.child("lname").getValue();
                    Glide.with(ProfileActivity.this).load(uri_profile).centerCrop().into(prof_pic);
                    displayname.setText(name + " " + lname);
                    ed_displayName.setHint(name);
                    ed_lastName.setHint(lname);
                    email.setText(mAuth.getCurrentUser().getEmail());
                    System.out.println(dataSnapshot.child("profileimage").getValue().toString());
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void AuthListener() {
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_REQUEST && resultCode == RESULT_OK) {
            imgUri = data.getData();
            Glide.with(getApplicationContext()).load(imgUri).centerCrop().error(R.drawable.image_not_available).into(prof_pic);
            CropImage.activity(imgUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);
            dialog.dismiss();
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (requestCode == RESULT_OK) {

                imgUri = result.getUri();

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception exception = result.getError();
            }
        }
    }

    public void update_profile() {
        if (mAuth.getCurrentUser() != null) {
            dialog.show();
            final String uid = mAuth.getCurrentUser().getUid();
            mDB_users.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!TextUtils.isEmpty(ed_displayName.getText().toString().trim())) {
                        mDB_users.child(uid).child("name").setValue(ed_displayName.getText().toString().trim());
                    }
                    if (!TextUtils.isEmpty(ed_lastName.getText().toString().trim())) {
                        mDB_users.child(uid).child("lname").setValue(ed_lastName.getText().toString().trim());

                    }
                    if (imgUri != null) {
                        StorageReference filepath = storage.child(imgUri.getLastPathSegment());
                        filepath.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String downloadUri = taskSnapshot.getDownloadUrl().toString();
                                mDB_users.child(uid).child("profileimage").setValue(downloadUri);
                            }
                        });
                    }
                    dialog.dismiss();
                    Intent i = new Intent(ProfileActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                }
            });
        }


    }
}
