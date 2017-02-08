package com.example.codetribe.movingservices;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.text.DecimalFormat;
import java.util.Calendar;

import dmax.dialog.SpotsDialog;

public class Send_RequestActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener {

    private TextView txtLocation_to, txtLocation_from, txtDate,txtTime, txtDistance;
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
    private DatabaseReference mDB_request;
    private FirebaseAuth mAuth;
    private LovelyStandardDialog lovelyStandardDialog;
    private SpotsDialog dialog;
    private String post_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send__request);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.req_map);
        mapFragment.getMapAsync(Send_RequestActivity.this);

        dialog = new SpotsDialog(this);
        mDB_request = FirebaseDatabase.getInstance().getReference().child("Request").push();
        mAuth = FirebaseAuth.getInstance();
        txtLocation_to = (TextView) findViewById(R.id.req_location_to);
        txtLocation_to.setClickable(true);
        txtLocation_to.setEnabled(false);
        txtLocation_from = (TextView) findViewById(R.id.req_location_from);
        txtLocation_from.setClickable(true);
        txtDate = (TextView) findViewById(R.id.req_date);
        txtTime =(TextView)findViewById(R.id.req_time);
        txtDate.setClickable(true);
        txtDistance = (TextView) findViewById(R.id.req_distance);
        edDesc = (EditText) findViewById(R.id.req_desc);
        btn_send = (Button) findViewById(R.id.btn_request);

        post_ID = getIntent().getStringExtra("Post_key");
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
public void timePicker(){

    final Calendar c = Calendar.getInstance();
    mHour = c.get(Calendar.HOUR_OF_DAY);
    mMinute = c.get(Calendar.MINUTE);
    TimePickerDialog timePickerDialog = new TimePickerDialog(Send_RequestActivity.this, new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            txtTime.setText(i+":"+i1);
        }
    },mHour,mMinute,false);
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
                txtDate.setText(i2 + "-" + i1 + "-" + i);

            }
        }, mYear, mMonth, mDay);
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
        //String to display current latitude and longitude
        String msg = latitude + ", " + longitude;
        Geocoder geocoder;
        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);

        //Adding marker to map
        mMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(true) //Making the marker draggable
                .title("Current Location")); //Adding a title

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //Displaying current coordinates in toast
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        double radiusInMeters = 100.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill
        mMap = googleMap;
        //Creating a random coordinate
        LatLng latLng = new LatLng(-34, 151);
        //Adding marker to that coordinate
       // mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        mMap.addCircle(new CircleOptions().center(latLng).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8));
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
                mMap.addMarker(new MarkerOptions().position(latlng_to).title("I am moving to").snippet(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_to)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng_to));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                //mMap.addPolyline(new PolylineOptions().add(latlng_from,latlng_to).width(5).color(R.color.colorAccent));
                mMap.addPolygon(new PolygonOptions().add(latlng_from, latlng_to));
                if(!TextUtils.isEmpty(txtLocation_from.getText().toString().trim())) {
                    txtDistance.setText(getDistance(latlng_from, latlng_to)+""+  CalculationByDistance(latlng_from, latlng_to));
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

                place2 = PlacePicker.getPlace(data, this);
                String location = String.format("%s", place2.getAddress());
                latlng_from = place2.getLatLng();
                txtLocation_from.setText(location);
                mMap.addMarker(new MarkerOptions().position(latlng_from).title("I am moving from").snippet(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_from)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng_from));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                txtLocation_to.setEnabled(true);
                if(!TextUtils.isEmpty(txtLocation_to.getText().toString().trim())) {
                    txtDistance.setText(getDistance(latlng_from, latlng_to));
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

    private String getDistance(LatLng my_latlong, LatLng frnd_latlong) {
        Location l1 = new Location("One");
        l1.setLatitude(my_latlong.latitude);
        l1.setLongitude(my_latlong.longitude);

        Location l2 = new Location("Two");
        l2.setLatitude(frnd_latlong.latitude);
        l2.setLongitude(frnd_latlong.longitude);

        float distance = l1.distanceTo(l2);
        String dist = distance + " M";

        if (distance > 1000.0f) {
            distance = distance / 1000.0f;
            dist = distance + " KM";
        }
        return dist;
    }
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    private void sendRequest() {

        if(mAuth.getCurrentUser() != null)
        {
            dialog.show();
            final String uid = mAuth.getCurrentUser().getUid();
            final String location_to = txtLocation_to.getText().toString().trim();
            final String location_from = txtLocation_from.getText().toString().trim();
            final String date_move = txtDate.getText().toString().trim();
            final String request_desc = edDesc.getText().toString().trim();
            mDB_request.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mDB_request.child("location_to").setValue(location_to);
                    mDB_request.child("location_from").setValue(location_from);
                    mDB_request.child("move_date").setValue(date_move);
                    mDB_request.child("user_id").setValue(uid);
                    mDB_request.child("latlng_from").setValue(latlng_from);
                    mDB_request.child("latlng_to").setValue(latlng_to);
                    mDB_request.child("desc_req").setValue(request_desc);
                    mDB_request.child("post_id").setValue(post_ID);
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                }
            });
        }
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


}
