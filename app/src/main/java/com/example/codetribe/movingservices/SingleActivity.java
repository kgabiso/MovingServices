package com.example.codetribe.movingservices;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import de.hdodenhof.circleimageview.CircleImageView;

public class SingleActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private TextView username, title, description, phoneNumber, email, location, likes, dislikes, price, single_send_request;
    private ImageView profilePic, main_image, img_dial;
    private ImageButton img_like, img_dislike;
    private FloatingActionButton delete_btn, fb_dial, fb, fb_send;
    private CircleImageView pro_pic;
    private DatabaseReference mDataRef;
    private DatabaseReference mDataLike;
    private DatabaseReference mDataDislike;
    private DatabaseReference mDataProfile;
    private DatabaseReference mDataRequest;
    private FirebaseAuth mAuth;
    private LinearLayout lin_email, lin_phone, location_finder;
    private String single_Post_key;
    private boolean likeProgress = false;
    private boolean DislikeProgress = false;
    private GoogleMap mMap;
    double single_post_lat;
    double single_post_long;
    String single_post_title, single_post_location;
    String sing_post_price;
    String single_post_uID;
    double lat, lng;
    LovelyStandardDialog lovelyStandardDialog;
    CollapsingToolbarLayout collapsingToolbarLayout;
    private boolean mClick = true;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private RotateAnimation rotate_clock, rotate_antiClock;
    private static String Single = "SingleActivity";

    public SingleActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        lovelyStandardDialog = new LovelyStandardDialog(this);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            single_Post_key = getIntent().getExtras().getString("Single_Post_id");
        }
        mDataRef = FirebaseDatabase.getInstance().getReference().child("Moving_Services");
        mDataLike = FirebaseDatabase.getInstance().getReference().child("likes");
        mDataDislike = FirebaseDatabase.getInstance().getReference().child("Dislikes");
        mDataProfile = FirebaseDatabase.getInstance().getReference().child("users");
        mDataRequest = FirebaseDatabase.getInstance().getReference().child("Request");
        //keep synced
        mDataDislike.keepSynced(true);
        mDataProfile.keepSynced(true);
        mDataRef.keepSynced(true);
        mDataProfile.keepSynced(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.main_collapsing);

        price = (TextView) findViewById(R.id.txtPrice);
        username = (TextView) findViewById(R.id.single_username);
        title = (TextView) findViewById(R.id.single_Title);
        description = (TextView) findViewById(R.id.single_Desc);
        phoneNumber = (TextView) findViewById(R.id.single_ContactNumber);
        email = (TextView) findViewById(R.id.single_ContactEmail);
        location = (TextView) findViewById(R.id.single_location);
        likes = (TextView) findViewById(R.id.single_Likes);
        dislikes = (TextView) findViewById(R.id.single_Dislikes);
        fb_dial = (FloatingActionButton) findViewById(R.id.single_fb_dial);
        fb = (FloatingActionButton) findViewById(R.id.single_fb);
        fb_send = (FloatingActionButton) findViewById(R.id.single_fb_send);
        pro_pic = (CircleImageView) findViewById(R.id.single_profile_image);
        lin_email = (LinearLayout) findViewById(R.id.single_lin_email);
        lin_phone = (LinearLayout) findViewById(R.id.single_lin_phone);
        main_image = (ImageView) findViewById(R.id.main_backdrop);
        profilePic = (ImageView) findViewById(R.id.single_profile_image);
        img_dislike = (ImageButton) findViewById(R.id.single_img_dislikes);
        img_like = (ImageButton) findViewById(R.id.single_img_likes);
        img_dial = (ImageView) findViewById(R.id.single_dial);
        single_send_request = (TextView) findViewById(R.id.single_send_request);
        location_finder = (LinearLayout) findViewById(R.id.location_finder);
        //+++++++++++++++++animations===========================
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++
        fb_send.setVisibility(View.INVISIBLE);
        fb_dial.setVisibility(View.INVISIBLE);
        set_likes(single_Post_key);
        set_Profile(mAuth.getCurrentUser().getUid().toString());
        location_finder.setClickable(true);
        location_finder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                i.putExtra("Single",Single);
                i.putExtra("Latitude", single_post_lat);
                i.putExtra("Longitude", single_post_long);
                i.putExtra("location", single_post_location);
                i.putExtra("title", single_post_title);
                i.putExtra("price", sing_post_price);
                startActivity(i);
            }
        });
        numberRequest();
        single_send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        img_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeProgress = true;
                mDataLike.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (likeProgress) {
                            if (dataSnapshot.child(single_Post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                mDataLike.child(single_Post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                likeProgress = false;
                            } else {
                                mDataLike.child(single_Post_key).child(mAuth.getCurrentUser().getUid()).setValue("Likes");
                                likeProgress = false;
                            }
                            //check if user already liked
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        img_dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DislikeProgress = true;
                mDataDislike.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (DislikeProgress) {
                            if (dataSnapshot.child(single_Post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                mDataDislike.child(single_Post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                DislikeProgress = false;
                            } else {
                                mDataDislike.child(single_Post_key).child(mAuth.getCurrentUser().getUid()).setValue("Dislikes");
                                DislikeProgress = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        lin_phone.setClickable(true);
        lin_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!phoneNumber.getText().toString().isEmpty()) {
                    Dial(phoneNumber.getText().toString().trim());
                } else {
                    Toast.makeText(getApplicationContext(), "No number found", Toast.LENGTH_LONG).show();
                }
            }
        });
        lin_email.setClickable(true);
        lin_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!email.getText().toString().isEmpty()) {
                    sendEmail(email.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "No email found", Toast.LENGTH_LONG).show();
                }
            }
        });
        img_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneNumber.getText().toString().isEmpty()) {
                    Dial(phoneNumber.getText().toString().trim());
                } else {
                    Toast.makeText(getApplicationContext(), "No number found", Toast.LENGTH_LONG).show();
                }
            }
        });
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClick) {
                    open_Fab();
                    mClick = false;
                } else {
                    close_fab();
                    mClick = true;
                }
            }
        });
        fb_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close_fab();
                mClick = true;
                Intent i = new Intent(getApplicationContext(), Send_RequestActivity.class);
                i.putExtra("Post_key", single_Post_key);
                i.putExtra("owerID", single_post_uID);
                i.putExtra("price", sing_post_price);
                startActivity(i);
            }
        });
        fb_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneNumber.getText().toString().isEmpty()) {
                    Dial(phoneNumber.getText().toString().trim());
                } else {
                    Toast.makeText(getApplicationContext(), "No number found", Toast.LENGTH_LONG).show();
                }
                close_fab();
                mClick = true;
            }
        });
        delete_btn = (FloatingActionButton) findViewById(R.id.single_delete);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lovelyStandardDialog
                        .setTopColorRes(R.color.colorAccent)
                        .setButtonsColorRes(R.color.colorPrimaryDark)
                        .setIcon(R.drawable.delivery_truck_icon)
                        .setTitle("Delete alert")
                        .setMessage("Are you sure you want to delete post")
                        .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDataRef.child(single_Post_key).removeValue();
                                Intent mainIntent = new Intent(SingleActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        mDataRef.child(single_Post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                single_post_title = (String) dataSnapshot.child("title").getValue();
                single_post_location = (String) dataSnapshot.child("location").getValue();
                String single_post_description = (String) dataSnapshot.child("description").getValue();
                String single_post_username = (String) dataSnapshot.child("username").getValue();
                String single_post_phone = (String) dataSnapshot.child("contactNumber").getValue();
                String single_post_email = (String) dataSnapshot.child("emailAddress").getValue();
                single_post_lat = (double) dataSnapshot.child("latlong").child("latitude").getValue();
                single_post_long = (double) dataSnapshot.child("latlong").child("longitude").getValue();
                String single_post_image = (String) dataSnapshot.child("image").getValue();
                single_post_uID = (String) dataSnapshot.child("uid").getValue();
                String single_post_propic = (String) dataSnapshot.child("profile").getValue();
                sing_post_price = (String) dataSnapshot.child("price").getValue();

                collapsingToolbarLayout.setTitle(single_post_title);
                collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.lightPrimary)); // transperent color = light primary
                collapsingToolbarLayout.setCollapsedTitleTextColor(Color.rgb(255, 255, 255)); //Color of your title
                price.setText("R " + sing_post_price + "/km");
                username.setText(single_post_username);
                title.setText(single_post_title);
                description.setText(single_post_description);
                phoneNumber.setText(single_post_phone);
                email.setText(single_post_email);
                location.setText(single_post_location);
                Glide.with(SingleActivity.this).load(single_post_propic).centerCrop().into(pro_pic);
                Glide.with(SingleActivity.this).load(single_post_image).centerCrop().into(main_image);
                if (mAuth.getCurrentUser().getUid().equals(single_post_uID)) {
                    delete_btn.setVisibility(View.VISIBLE);
                } else {
                    delete_btn.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        lat = single_post_lat;
        lng = single_post_long;
    }

    public void numberRequest() {
        mDataRequest.orderByChild("post_id").equalTo(single_Post_key).addValueEventListener(new ValueEventListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int num = (int) dataSnapshot.getChildrenCount();
                single_send_request.setText("Request (" + num + ")");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void open_Fab() {
        rotate_clock = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate_clock.setFillAfter(true);
        rotate_clock.setDuration(300);
        rotate_clock.setInterpolator(new AccelerateDecelerateInterpolator());

        fb_send.setVisibility(View.VISIBLE);
        fb_dial.setVisibility(View.VISIBLE);
        fb_send.setClickable(true);
        fb_dial.setClickable(true);
        fb.startAnimation(rotate_clock);
        fb_send.startAnimation(fab_open);
        fb_dial.startAnimation(fab_open);
    }

    public void close_fab() {
        rotate_antiClock = new RotateAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate_antiClock.setFillAfter(true);
        rotate_antiClock.setDuration(300);
        rotate_antiClock.setInterpolator(new AccelerateDecelerateInterpolator());

        fb_send.setVisibility(View.INVISIBLE);
        fb_dial.setVisibility(View.INVISIBLE);
        fb_send.setClickable(false);
        fb_dial.setClickable(false);
        fb.startAnimation(rotate_antiClock);
        fb_send.startAnimation(fab_close);
        fb_dial.startAnimation(fab_close);
    }

    protected void sendEmail(String To) {
        Log.i("Send email", "");
        String[] TO = {To};
        String[] CC = {""};
        Intent intentEmail = new Intent(Intent.ACTION_SENDTO);
        intentEmail.setData(Uri.parse("mailto:"));
        intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{To});
        intentEmail.putExtra(intentEmail.EXTRA_SUBJECT, "Moving Services ");
        intentEmail.putExtra(intentEmail.EXTRA_TEXT, "Message goes here");
        if (intentEmail.resolveActivity(getPackageManager()) != null) {

        }

        try {
            startActivity(intentEmail);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(SingleActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void Dial(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber.trim()));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(callIntent);
    }

    public void set_likes(final String post_key) {

        mDataLike.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                    // img_like.setImageResource(R.drawable.ic_mood_active_24dp1);
                    likes.setText("Likes " + (int) dataSnapshot.child(post_key).getChildrenCount());
                    likes.setTextColor(ContextCompat.getColor(SingleActivity.this, R.color.accept));
                    img_dislike.setEnabled(false);
                } else {
                    // img_like.setImageResource(R.drawable.ic_mood_black_24dp);
                    likes.setText("Likes " + (int) dataSnapshot.child(post_key).getChildrenCount());
                    likes.setTextColor(ContextCompat.getColor(SingleActivity.this, R.color.editText));
                    img_dislike.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDataDislike.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                    //img_dislike.setImageResource(R.drawable.ic_mood_bad_active_24dp1);
                    dislikes.setText("Dislikes " + (int) dataSnapshot.child(post_key).getChildrenCount());
                    dislikes.setTextColor(ContextCompat.getColor(SingleActivity.this, R.color.reject));
                    img_like.setEnabled(false);
                } else {
                    //img_dislike.setImageResource(R.drawable.ic_mood_bad_black_24dp);
                    dislikes.setText("Dislikes " + (int) dataSnapshot.child(post_key).getChildrenCount());
                    dislikes.setTextColor(ContextCompat.getColor(SingleActivity.this, R.color.editText));
                    img_like.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void set_Profile(final String uid) {
        mDataProfile.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(uid).hasChild(mAuth.getCurrentUser().getUid())) {
                    String url = dataSnapshot.child("profileimage").toString();
                    Glide.with(SingleActivity.this).load(url).centerCrop().into(profilePic);
                    Toast.makeText(getApplicationContext(), uid, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // LatLng position = new LatLng(lat, lng);
        // Add a marker in Sydney and move the camera

//        mMap.addMarker(new MarkerOptions().position(position).title("Marker in Sydney").snippet("check out the place").draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
