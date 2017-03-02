package com.example.codetribe.movingservices;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.text.DateFormat;
import java.util.Calendar;

import dmax.dialog.SpotsDialog;


public class AddPostActivity extends AppCompatActivity implements OnItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private FloatingActionButton fb_chooseimg;
    private ImageButton img_choose;
    private ImageView img_contacPicker, img_locationPicker;
    private EditText ed_title, ed_Desc, ed_ContactNumber, ed_Email, ed_price;
    TextView example;
    private TextView ed_location;
    private Spinner vehicle_type;
    private Button bt_Post;
    private static final int Galleery_REQUEST = 1;
    public static final int RESULT_PICK_CONTACT = 8550;
    Uri imgUri = null;
    String message = "";
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mDatabaseUser;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String mVehicle_type;
    private SpotsDialog dialog;
    /****************
     * location
     ******************/
    int PLACE_PICKER_REQUEST = 2;
    private GoogleApiClient mGoogleApiClient;
    LatLng latLng1 = null;
    LatLng latLng2 = null;
    private GoogleMap mMap;
    String TAG = "PlaceAutocompleteFragment";

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {//On Create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        dialog = new SpotsDialog(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        example = (TextView) findViewById(R.id.textexample);
        vehicle_type = (Spinner) findViewById(R.id.spinner_vehicle);
        ed_price = (EditText) findViewById(R.id.textPricing);
        img_contacPicker = (ImageView) findViewById(R.id.contactPicker);
        img_locationPicker = (ImageView) findViewById(R.id.img_location_picker);
        bt_Post = (Button) findViewById(R.id.btn_Post);
        fb_chooseimg = (FloatingActionButton) findViewById(R.id.fb_picture);
        img_choose = (ImageButton) findViewById(R.id.imgPost);
        ed_title = (EditText) findViewById(R.id.textTitle);
        ed_Desc = (EditText) findViewById(R.id.textDesc);
        ed_ContactNumber = (EditText) findViewById(R.id.textContactNumber);
        ed_Email = (EditText) findViewById(R.id.textContactEmail);
        ed_location = (TextView) findViewById(R.id.textLocation);
        Glide.with(getApplicationContext()).load(R.drawable.image_not_available).asGif().centerCrop().error(R.drawable.image_not_available).into(img_choose);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Moving_Services");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        img_contacPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactIntent, RESULT_PICK_CONTACT);
            }
        });
        fb_chooseimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Galleery_REQUEST);
            }
        });
        img_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Galleery_REQUEST);
            }
        });
        bt_Post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startingPost();
            }
        });
        img_locationPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*=========== location picker for location icon =================*/
                Toast.makeText(getApplication(), "loading Google maps ...", Toast.LENGTH_SHORT).show();
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(AddPostActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Toast.makeText(AddPostActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(AddPostActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        ed_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 /*=========== location picker for location TextView=================*/
                Toast.makeText(getApplication(), "loading Google maps ...", Toast.LENGTH_SHORT).show();
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {

                    startActivityForResult(builder.build(AddPostActivity.this), PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Toast.makeText(AddPostActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(AddPostActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

        });


    }

    private void startingPost() {
        // progressBar.setVisibility(View.VISIBLE);
        dialog.show();
        final String mTitle = ed_title.getText().toString();
        final String mDesc = ed_Desc.getText().toString();
        final String mContactNumber = ed_ContactNumber.getText().toString();
        final String mEmail = ed_Email.getText().toString();
        final String mLoction = ed_location.getText().toString();
        final String mPrice = ed_price.getText().toString();
        final String mDate = DateFormat.getDateInstance(DateFormat.SHORT).format(Calendar.getInstance().getTime());
        Toast.makeText(getApplicationContext(), mDate, Toast.LENGTH_SHORT).show();
        if (vehicle_type.getSelectedItemPosition() != 0) {
            mVehicle_type = vehicle_type.getSelectedItem().toString();
        } else {
            mVehicle_type = "";
        }
        vehicle_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                example.setText(message);
            }
        });
        if (!TextUtils.isEmpty(mTitle) && !TextUtils.isEmpty(mDesc) && !TextUtils.isEmpty(mContactNumber) && !TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mLoction) && !TextUtils.isEmpty(mPrice) && imgUri != null) {

            final StorageReference filepath = mStorageRef.child("Truck_images").child(imgUri.getLastPathSegment()/*name of image*/);
            filepath.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUri = taskSnapshot.getDownloadUrl();// get img URI

                    final DatabaseReference newPost = mDatabaseRef.push();

                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // progressBar.setVisibility(View.VISIBLE);
                            newPost.child("title").setValue(mTitle);
                            newPost.child("description").setValue(mDesc);
                            newPost.child("contactNumber").setValue(mContactNumber);
                            newPost.child("emailAddress").setValue(mEmail);
                            newPost.child("location").setValue(mLoction);
                            newPost.child("latlong").setValue(latLng1);
                            newPost.child("image").setValue(downloadUri.toString());
                            newPost.child("uid").setValue(currentUser.getUid());
                            newPost.child("price").setValue(mPrice);
                            newPost.child("vehicleType").setValue(mVehicle_type);
                            newPost.child("postdate").setValue(mDate);
                            newPost.child("profile").setValue(dataSnapshot.child("profileimage").getValue());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(AddPostActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error Posting", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            });
        } else {

            dialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Place place = null;
        if (requestCode == Galleery_REQUEST && resultCode == RESULT_OK) {
            imgUri = data.getData();
            Glide.with(getApplicationContext()).load(imgUri).centerCrop().error(R.drawable.image_not_available).into(img_choose);
            CropImage.activity(imgUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16, 9)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Glide.with(getApplicationContext()).load(resultUri).centerCrop().error(R.drawable.image_not_available).into(img_choose);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        if (resultCode == RESULT_OK) {
            //check for the request code ,we might be using multiple startActivityResult
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;


            }
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("%s", place.getAddress());
                latLng1 = place.getLatLng();
                ed_location.setText(toastMsg);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

    }

    private void contactPicked(Intent data) {
        Cursor cursorContact = null;
        try {
            String phoneNumber = null;
            Uri uri = data.getData();
            //Query the contact uri
            cursorContact = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
            cursorContact.moveToFirst();
            //column index of the phone number
            int phoneIndex = cursorContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            phoneNumber = cursorContact.getString(phoneIndex);
            ed_ContactNumber.setText(phoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


        switch (i) {
            case 0:
                message = "please select a vehicle";
                break;
            case 1:
                message = "- eg.Citroen Nemo, Peugeot Bipper and Similar (500-600kg)";
                break;
            case 2:
                message = "- eg.Ford Transit Connect, Vauxhall Combo and Similar (600-900kg)";
                break;
            case 3:
                message = "-eg.Ford Transit SWB, Volkswagen Transporter SWB or Renault Trafic (900-1200kg)";
                break;
            case 4:
                message = " -eg.FORD TRANSIT LUTON TAIL,Mercedes Luton van,MERCEDES SPRINTER LUTON VAN (1000–1200kg)";
                break;
            case 5:
                message = "-eg.Isuzu FTR800 Dropside,TATA 709 DROPSIDE TRUCK,MITSUBISHI FUSO DROPSIDES (Approx. 1500kg)";
                break;
            case 6:
                message = "-eg.Ford Ranger, Mitsubishi L200, Isuzu D–Max and Similar (1,000–1,100kg)";
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
