package com.example.codetribe.movingservices;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

@TargetApi(Build.VERSION_CODES.N)
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DirectionCallback {

    private GoogleMap googleMap;
    private String serverKey = "AIzaSyCOuDlu_I4fGpPlulUrUHtC-9t-9zoy2pE";
    private double lng,lat;
    private double long_from,long_to,lat_from,lat_to;
    private double longitude,latitude;
    private String location,title;
    private double price;
    private String post_key;
    private FloatingActionButton location_picker;
    private LatLng origin ;
    private LatLng destination;
    GoogleApiClient googleApiClient;
    LovelyStandardDialog lovelyStandardDialog;
    private SpotsDialog dialog;
   private TextView txt_distance,txt_time,txt_travel_time;
    private int routecount = 0;
    private CircleImageView markerImageView;
    private String uriProfile;
    private FirebaseAuth mAuth;
    private DatabaseReference mDB_users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fullmap);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        lovelyStandardDialog = new LovelyStandardDialog(this);
        dialog = new SpotsDialog(this);
        txt_distance = (TextView)findViewById(R.id.map_distance);
        txt_time =(TextView)findViewById(R.id.map_time);
        txt_travel_time =(TextView)findViewById(R.id.map_travel_time);
        location_picker =(FloatingActionButton)findViewById(R.id.fb_location_picker);
        txt_time.setText("Time travelled : 0 min");
        txt_distance.setText("Distance : 0 km");
        txt_travel_time.setText("Pick up location \nDrop location :");
        mAuth = FirebaseAuth.getInstance();
        mDB_users = FirebaseDatabase.getInstance().getReference().child("users");
        mDB_users.keepSynced(true);
        //---------

        lat = getIntent().getExtras().getDouble("Latitude");
        lng = getIntent().getExtras().getDouble("Longitude");
        location = getIntent().getExtras().getString("location");
        title = getIntent().getExtras().getString("title");

        lat_from = getIntent().getExtras().getDouble("latitude_from");
        long_from = getIntent().getExtras().getDouble("longitude_from");
        lat_to = getIntent().getExtras().getDouble("latitude_to");
        long_to = getIntent().getExtras().getDouble("longitude_to");
        origin = new  LatLng(long_from,lat_from);
        destination = new LatLng(long_to, lat_to);
        post_key = getIntent().getExtras().getString("post_key");

        //==========
        location_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(routecount <= 2) {

                    requestDirection();

                }else {
                    routecount = 0;
                    requestDirection();
                }
            }
        });
        getCurrentLocation();
        getProfile_Uri();
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
            moveMap();
            getProfile_Uri();
        }
    }
    //Function to move the map
    private void moveMap() {
        double radiusInMeters = 1000.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000;
        //String to display current latitude and longitude
        String msg = latitude + ", " + longitude;
        Geocoder geocoder;
        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);

        //Adding marker to map
        googleMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(true) //Making the marker draggable
                .title("Current Location").icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(uriProfile))));//Adding a title
        googleMap.addCircle(new CircleOptions().center(latLng).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8));
        //Moving the camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Animating the camera
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        googleMap.addMarker(new MarkerOptions()
                .position(origin) //setting position
                .draggable(true) //Making the marker draggable
                .title("Moving from"));//Adding a title

        googleMap.addMarker(new MarkerOptions()
                .position(destination) //setting position
                .draggable(true) //Making the marker draggable
                .title("Moving to"));//Adding a title
    }
    public void requestDirection() {
        dialog.show();
        dialog.setMessage("getting Map Directions");

        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .alternativeRoute(true)
                .execute(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng latLng = new LatLng(-34, 151);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Animating the camera
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    private Bitmap getMarkerBitmapFromView(String resId) {


        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custome_marker_layout, null);
        markerImageView = (CircleImageView) customMarkerView.findViewById(R.id.profile_image);
        //markerImageView.setImageResource(resId);
        //markerImageView.setImageURI(uri);
        Glide.with(getApplicationContext()).load(resId).error(R.drawable.ic_account_circle_black_24dp).centerCrop().into(markerImageView);
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
    private void getProfile_Uri() {

        String uid = mAuth.getCurrentUser().getUid();
        if (mAuth.getCurrentUser().getUid() != null) {
            mDB_users.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    uriProfile = (String) dataSnapshot.child("profileimage").getValue();
                    //Glide.with(getApplication()).load(uriProfile).centerCrop().into(markerImageView);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

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
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {


            String time = null;
            String Distance = null;
            String Start_Location = null;
            String End_location = null;
            if (routecount == 0) {
                googleMap.clear();
                int color = R.color.route;
                ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                Distance = String.valueOf(direction.getRouteList().get(0).getLegList().get(0).getDistance().getText());
                time = String.valueOf(direction.getRouteList().get(0).getLegList().get(0).getDuration().getText());
                Start_Location = String.valueOf(direction.getRouteList().get(0).getLegList().get(0).getStartAddress());
                End_location = String.valueOf(direction.getRouteList().get(0).getLegList().get(0).getEndAddress());
                location_picker.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(color)));
                //location_picker.setBackgroundTintList(ColorStateList.valueOf(R.color.route));
                googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, getResources().getColor(color)));
            } else {
                googleMap.clear();
                int[] colors = {R.color.altern_rout, R.color.altern_rout1, R.color.altern_rout2};
                Route route = direction.getRouteList().get(routecount);
                int color = colors[routecount];
                ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                Distance = String.valueOf(direction.getRouteList().get(routecount).getLegList().get(0).getDistance().getText());
                time = String.valueOf(direction.getRouteList().get(routecount).getLegList().get(0).getDuration().getText());
                Start_Location = String.valueOf(direction.getRouteList().get(0).getLegList().get(0).getStartAddress());
                End_location = String.valueOf(direction.getRouteList().get(0).getLegList().get(0).getEndAddress());
                location_picker.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(color)));
                googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, getResources().getColor(color)));
                Toast.makeText(getApplicationContext(), "Alternative Route " + routecount, Toast.LENGTH_SHORT).show();

            }
            txt_time.setText("Time travelled : " + time);
            txt_distance.setText("Distance : " + Distance);
            txt_travel_time.setText("Pick up location : " +
                    Start_Location + "\nDrop location : " + End_location);
            routecount++;
            getCurrentLocation();
            googleMap.addMarker(new MarkerOptions().position(origin).title("Moving from").snippet(Start_Location));
            googleMap.addMarker(new MarkerOptions().position(destination).title("Moving to").snippet(End_location));
            dialog.dismiss();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
    }
}
