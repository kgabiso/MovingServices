package com.example.codetribe.movingservices;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class Send_RequestActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener, DirectionCallback {

    private String serverKey = "AIzaSyCOuDlu_I4fGpPlulUrUHtC-9t-9zoy2pE";
    private TextView txtLocation_to, txtLocation_from, txtDate, txtTime, txtDistance;
    private GoogleMap mMap;
    private EditText edDesc;
    private Button btn_send;
    private int mYear, mMonth, mDay, mHour, mMinute;
    int PLACE_PICKER_REQUEST_FROM = 1;
    int PLACE_PICKER_REQUEST_TO = 2;
    GoogleApiClient googleApiClient;
    private double longitude;
    private double latitude;
    private LatLng latlng_from, latlng_to;
    private DatabaseReference mDB_request, mDB_users;
    private FirebaseAuth mAuth;
    private LovelyStandardDialog lovelyStandardDialog;
    private SpotsDialog dialog;
    private String post_ID, uriProfile, owner_id, price_km;
    private CircleImageView markerImageView;
    private TextView txt_price, txt_travel_time;
    String origin, destination;
    String mydate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send__request);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.req_map);
        mapFragment.getMapAsync(Send_RequestActivity.this);


        mDB_request = FirebaseDatabase.getInstance().getReference().child("Request").push();
        mDB_users = FirebaseDatabase.getInstance().getReference().child("users");
        mDB_users.keepSynced(true);
        mDB_request.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog(this);
        txt_price = (TextView) findViewById(R.id.req_price);
        txt_travel_time = (TextView) findViewById(R.id.req_travel_time);
        txtLocation_to = (TextView) findViewById(R.id.req_location_to);
        txtLocation_to.setClickable(true);
        txtLocation_to.setEnabled(false);
        txtLocation_from = (TextView) findViewById(R.id.req_location_from);
        txtLocation_from.setClickable(true);
        txtDate = (TextView) findViewById(R.id.req_date);
        txtTime = (TextView) findViewById(R.id.req_time);
        txtDate.setClickable(true);
        txtDistance = (TextView) findViewById(R.id.req_distance);
        edDesc = (EditText) findViewById(R.id.req_desc);
        btn_send = (Button) findViewById(R.id.btn_request);
        mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        getProfile_Uri();
        post_ID = getIntent().getStringExtra("Post_key");
        owner_id = getIntent().getStringExtra("owerID");
        price_km = getIntent().getStringExtra("price");
        lovelyStandardDialog = new LovelyStandardDialog(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        txtLocation_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationpicker_to();
            }
        });

        txtLocation_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getCurrentLocation();
//                moveMap();
                locationpicker_from();
            }
        });
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker();
            }
        });
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker();
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();

            }
        });


    }

    public void requestDirection(LatLng origin, LatLng destination) {
        dialog.show();
        dialog.setMessage("getting Map Directions");

        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);

    }

    public void timePicker() {

        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(Send_RequestActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                txtTime.setText(i + ":" + i1);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void datePicker() {
        //using calendar to show the current date
        final Calendar c = Calendar.getInstance(); //for the calender import java.util.Calendar; not android.icu.util
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(Send_RequestActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i/*year*/, int i1/*month*/, int i2/*day*/) {


                    txtDate.setText(i2 + "-" + (i1+1) + "-" + i);

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void locationpicker_from() {
        dialog.show();
        dialog.setMessage("Loading google location picker ...");
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(Send_RequestActivity.this), PLACE_PICKER_REQUEST_FROM);
            dialog.dismiss();
        } catch (GooglePlayServicesRepairableException e) {
            dialog.dismiss();
            e.printStackTrace();
            Toast.makeText(Send_RequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

        } catch (GooglePlayServicesNotAvailableException e) {
            dialog.dismiss();
            e.printStackTrace();
            Toast.makeText(Send_RequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

        }
    }

    private void locationpicker_to() {
        dialog.show();
        dialog.setMessage("Loading google location picker ...");
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            dialog.dismiss();
            startActivityForResult(builder.build(Send_RequestActivity.this), PLACE_PICKER_REQUEST_TO);
        } catch (GooglePlayServicesRepairableException e) {
            dialog.dismiss();
            e.printStackTrace();
            Toast.makeText(Send_RequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            dialog.dismiss();
            e.printStackTrace();
            Toast.makeText(Send_RequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        //Creating a location object
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            //moving the map to location
            moveMap();
        }
    }

    //Function to move the map
    private void moveMap() {
        double radiusInMeters = 100.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000;

        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);

        //Adding marker to map
        mMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(true) //Making the marker draggable
                .title("Current Location").icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(uriProfile)))); //Adding a title
        mMap.addCircle(new CircleOptions().center(latLng).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8));
        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //opaque red fill
        mMap = googleMap;
        //Creating a random coordinate
        LatLng latLng = new LatLng(-34, 151);
        //Adding marker to that coordinate
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(uriProfile))));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Setting onMarkerDragListener to track the marker drag
        mMap.setOnMarkerDragListener(this);
        //Adding a long click listener to the map
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Place place = null;
        Place place2 = null;

        if (requestCode == PLACE_PICKER_REQUEST_TO) {

            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);
                String location = String.format("%s", place.getAddress());
                latlng_to = place.getLatLng();
                txtLocation_to.setText(location);
                destination = location;
                mMap.addMarker(new MarkerOptions().position(latlng_to).title("I am moving to").snippet(destination));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng_to));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

                if (!TextUtils.isEmpty(txtLocation_from.getText().toString().trim())) {

                    requestDirection(latlng_from, latlng_to);
                }
            } else if (requestCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                lovelyStandardDialog
                        .setTopColorRes(R.color.colorAccent)
                        .setButtonsColorRes(R.color.colorPrimaryDark)
                        .setIcon(R.drawable.delivery_truck_icon)
                        .setTitle("Place picker Error")
                        .setMessage(status.getStatusMessage())
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
        if (requestCode == PLACE_PICKER_REQUEST_FROM) {
            if (resultCode == RESULT_OK) {
                mMap.clear();
                place2 = PlacePicker.getPlace(data, this);
                String location = String.format("%s", place2.getAddress());
                latlng_from = place2.getLatLng();
                txtLocation_from.setText(location);
                origin = location;
                mMap.addMarker(new MarkerOptions().position(latlng_from).title("I am moving from").snippet(origin));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng_from));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                txtLocation_to.setEnabled(true);

                if (!TextUtils.isEmpty(txtLocation_to.getText().toString().trim())) {

                    requestDirection(latlng_from, latlng_to);
                }
            } else if (requestCode == PlaceAutocomplete.RESULT_ERROR) {
                txtLocation_to.setEnabled(false);
                Status status = PlaceAutocomplete.getStatus(this, data);
                lovelyStandardDialog
                        .setTopColorRes(R.color.colorAccent)
                        .setButtonsColorRes(R.color.colorPrimaryDark)
                        .setIcon(R.drawable.delivery_truck_icon)
                        .setTitle("Place picker Error")
                        .setMessage(status.getStatusMessage())
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            } else if (requestCode == RESULT_CANCELED) {
                txtLocation_to.setEnabled(false);
            }
        }
    }

    private void getProfile_Uri() {

        String uid = mAuth.getCurrentUser().getUid();
        if (mAuth.getCurrentUser().getUid() != null) {
            mDB_users.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    uriProfile = (String) dataSnapshot.child("profileimage").getValue();
                    Glide.with(getApplication()).load(uriProfile).centerCrop().into(markerImageView);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void sendRequest() {

        if (mAuth.getCurrentUser() != null) {
            dialog.show();
            final String uid = mAuth.getCurrentUser().getUid();
            final String location_to = txtLocation_to.getText().toString().trim();
            final String location_from = txtLocation_from.getText().toString().trim();
            final String date_move = txtDate.getText().toString().trim();
            final String request_desc = edDesc.getText().toString().trim();
            final String Time_move = txtTime.getText().toString().trim();
            if (!TextUtils.isEmpty(location_from)) {
                if (!TextUtils.isEmpty(location_to)) {
                    if (!TextUtils.isEmpty(date_move) && !TextUtils.isEmpty(Time_move)) {
                        mDB_request.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                mDB_request.child("location_to").setValue(location_to);
                                mDB_request.child("location_from").setValue(location_from);
                                mDB_request.child("move_date").setValue(date_move);
                                mDB_request.child("move_time").setValue(Time_move);
                                mDB_request.child("user_id").setValue(uid);
                                mDB_request.child("owner_id").setValue(owner_id);
                                mDB_request.child("latlng_from").setValue(latlng_from);
                                mDB_request.child("latlng_to").setValue(latlng_to);
                                mDB_request.child("desc_req").setValue(request_desc);
                                mDB_request.child("post_id").setValue(post_ID);
                                mDB_request.child("request_date").setValue(mydate);
                                mDB_request.child("status").setValue("Pending");
                                dialog.dismiss();

                                Intent i = new Intent(Send_RequestActivity.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                dialog.dismiss();
                            }
                        });

                    } else {
                        Toast.makeText(getApplicationContext(), "You need to add date and of moving ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You need add location were you going to ", Toast.LENGTH_LONG).show();
                }

            } else {

                Toast.makeText(getApplicationContext(), "You need location were you going from ", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Bitmap getMarkerBitmapFromView(String resId) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custome_marker_layout, null);
        markerImageView = (CircleImageView) customMarkerView.findViewById(R.id.profile_image);
        //markerImageView.setImageResource(resId);
        //markerImageView.setImageURI(uri);
        Glide.with(getApplicationContext()).load(resId).centerCrop().into(markerImageView);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //Moving the map
        moveMap();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
//Clearing all the markers
        mMap.clear();
        //Adding a new marker to the current pressed position
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));
    }


    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latlng_from).title("I am moving from").snippet(origin));
            mMap.addMarker(new MarkerOptions().position(latlng_to).title("I am moving to").snippet(destination));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            String Distance = String.valueOf((direction.getRouteList().get(0).getLegList().get(0).getDistance().getText()));
            String time = String.valueOf(direction.getRouteList().get(0).getLegList().get(0).getDuration().getText());
            float dist_meters = Float.parseFloat(direction.getRouteList().get(0).getLegList().get(0).getDistance().getValue());
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLUE));

            txt_travel_time.setText("Time : " + time);
            txt_price.setText("Price : R" + getprice(dist_meters, Float.parseFloat(price_km)));
            txtDistance.setText("Distance : " + Distance);

            dialog.dismiss();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    public float getprice(float dist_meter, float price_km) {

        float TotalPrice = (dist_meter / 1000) * price_km;

        return TotalPrice;
    }
}
