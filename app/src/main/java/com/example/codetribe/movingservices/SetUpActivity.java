package com.example.codetribe.movingservices;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class SetUpActivity extends AppCompatActivity {

   private CircleImageView profilePic;
   private EditText firstName, lastName;
    private Button btn_submit;
    private static final int Galleery_REQUEST = 1;
    private FloatingActionButton profilebutton;
    Uri imgUri = null;
   private DatabaseReference  mDatabaseRef;
    private FirebaseAuth mAuth;
    private StorageReference storage;
    private SpotsDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);


        storage = FirebaseStorage.getInstance().getReference().child("profile_picture");
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");

        dialog = new SpotsDialog(this);
        profilebutton = (FloatingActionButton)findViewById(R.id.single_fb);
        profilePic = (CircleImageView)findViewById(R.id.profile_image);
        firstName =(EditText)findViewById(R.id.profile_name);
        lastName=(EditText)findViewById(R.id.last_name);
        btn_submit = (Button)findViewById(R.id.btn_submit);

        profilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Galleery_REQUEST);
                dialog.show();
            }
        });
        profilePic.setClickable(true);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Galleery_REQUEST);
                dialog.dismiss();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpAccount();
            }
        });
    }

    private void setUpAccount() {
        final String name = firstName.getText().toString().trim();
        final String lname = lastName.getText().toString().trim();
        final String userID = mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(name) && imgUri != null && !TextUtils.isEmpty(lname)){


            dialog.show();
            StorageReference filepath = storage.child(imgUri.getLastPathSegment());
            filepath.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    mDatabaseRef.child(userID).child("name").setValue(name);
                    mDatabaseRef.child(userID).child("lname").setValue(lastName);
                    mDatabaseRef.child(userID).child("profileimage").setValue(downloadUri);

                    Intent mainIntent = new Intent(SetUpActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();
                    dialog.dismiss();
                }
            });

        }
        else {
            Toast.makeText(getApplicationContext(),"Please enter Display name and Profile picture to complete ",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Galleery_REQUEST && resultCode == RESULT_OK) {
            imgUri = data.getData();
            Glide.with(getApplicationContext()).load(imgUri).centerCrop().error(R.drawable.image_not_available).into(profilePic);
            CropImage.activity(imgUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);
            dialog.dismiss();
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(requestCode == RESULT_OK){

                imgUri = result.getUri();

            }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception exception = result.getError();
            }
        }
    }
}
