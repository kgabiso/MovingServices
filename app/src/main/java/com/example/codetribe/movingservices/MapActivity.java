package com.example.codetribe.movingservices;

import android.annotation.TargetApi;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

@TargetApi(Build.VERSION_CODES.N)
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    String TAG = "PlaceAutocompleteFragment";
    private GoogleMap mMap;
    private double lng,lat;
    private String location,title;
    private double price;
    private FloatingActionButton location_picker;
    private Button calculate_distance;
    int PLACE_PICKER_REQUEST = 1;
    Place place = null;
    LatLng latLng1 = null;
    LatLng latLng2;
    LovelyStandardDialog lovelyStandardDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fullmap);
        mapFragment.getMapAsync(this);


        lovelyStandardDialog = new LovelyStandardDialog(this);
        calculate_distance = (Button)findViewById(R.id.btn_calculate);
        location_picker =(FloatingActionButton)findViewById(R.id.fb_location_picker);
        lat = getIntent().getExtras().getDouble("Latitude");
        lng = getIntent().getExtras().getDouble("Longitude");
        location = getIntent().getExtras().getString("location");
        title = getIntent().getExtras().getString("title");
        String strprice = getIntent().getExtras().getString("price");
        price = Double.parseDouble(strprice);
        calculate_distance.setEnabled(false);
        calculate_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(getDistance(latLng1,latLng2) != null) {
                    lovelyStandardDialog
                            .setTopColorRes(R.color.colorAccent)
                            .setButtonsColorRes(R.color.colorPrimaryDark)
                            .setIcon(R.drawable.delivery_truck_icon)
                            .setTitle("Distance")
                            .setMessage("Distance between the two locations\n" + getDistance(latLng1, latLng2))
                            .setNeutralButton(android.R.string.ok, null)

                            .show();
                }else {

                }
            }
        });
        location_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Toast.makeText(MapActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(MapActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String toastMsg = null;
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);
                toastMsg = String.format("Place: %s", place.getAddress());
                latLng1 = place.getLatLng();
                calculate_distance.setEnabled(true);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                calculate_distance.setEnabled(false);
            }
        }
        LatLng mylocation = new LatLng( latLng1.latitude, latLng1.longitude);

        mMap.addMarker(new MarkerOptions().position(mylocation).title("Selected place : " + toastMsg).snippet("My location").icon(BitmapDescriptorFactory.fromResource(R.drawable.pointing_down)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 10));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
       // LatLng loaction = new LatLng( lat,  lng);
        latLng2 = new LatLng( lat,  lng);
        mMap.addMarker(new MarkerOptions().position(latLng2).title(location).snippet(title).icon(BitmapDescriptorFactory.fromResource(R.drawable.delivery_truck_map)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 15));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng2));


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
        String totalPrice = String.valueOf(price * distance);
        if (distance > 1000.0f) {
            distance = distance / 1000.0f;
            dist = distance + " KM";
        }
        return dist +"\n R"+totalPrice;
    }

}
