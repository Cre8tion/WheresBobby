package com.example.wheresbobby;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {

    private GoogleMap mMap;
    Button buttonGoToSUTD;
    float ZOOM = 18;

    Marker mSUTDcanteen;
    Marker mSUTDlibrary;

    Button library_level_1;
    Button library_level_2;

    private BottomSheetBehavior mBottomSheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        /**
         * INTENT FOR BOTH LIBRARY BUTTONS ARE HERE
         */
        library_level_1 = findViewById(R.id.library_level_1);
        library_level_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        library_level_2 = findViewById(R.id.library_level_1);
        library_level_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.canteen){
            Intent intent = new Intent(MapsActivity.this, CanteenPanoActivity.class);
            startActivity(intent);
        }
        if(id == R.id.goToSUTD){
            Geocoder gc = new Geocoder(MapsActivity.this);
            try {
                List<Address> list = gc.getFromLocationName("SUTD", 1);
                Address address = list.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();
                LatLng sutd = new LatLng(lat, lng);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sutd, ZOOM));

            }catch (IOException ex){
                Toast.makeText(MapsActivity.this, "Location does not exist", Toast.LENGTH_SHORT).show();
            }
        }
        if(id == R.id.library){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMarkerClickListener(this);
        Geocoder gc = new Geocoder(this);
        try {
            List<Address> list = gc.getFromLocationName("SUTD", 1);
            Address address = list.get(0);
            double lat = address.getLatitude();
            double lng = address.getLongitude();
            LatLng sutd = new LatLng(lat, lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sutd, 18));
            list = gc.getFromLocationName("SUTD canteen", 1);
            address = list.get(0);
            lat = address.getLatitude();
            lng = address.getLongitude();
            sutd = new LatLng(lat, lng);

            mSUTDcanteen = mMap.addMarker(new MarkerOptions().position(sutd)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            );

            list = gc.getFromLocationName("SUTD library", 1);
            address = list.get(0);
            lat = address.getLatitude();
            lng = address.getLongitude();
            sutd = new LatLng(lat, lng);

            mSUTDlibrary = mMap.addMarker(new MarkerOptions().position(sutd)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            );
        }catch (IOException ex){
            Toast.makeText(MapsActivity.this, "Location does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker){
        if(marker.equals(mSUTDcanteen)|| marker.equals(mSUTDlibrary)){
            // This causes the marker at Perth to bounce into position when it is clicked.
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500;

            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(
                            1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker.setAnchor(0.5f, 1.0f + 2 * t);

                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        }
        /**
         * INTENT CALL HERE CANTEEN
         */
        if(marker.equals(mSUTDcanteen)){
            Intent intent = new Intent(MapsActivity.this, CanteenPanoActivity.class);
            startActivity(intent);
        }
        if(marker.equals(mSUTDlibrary)){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        return false;
    }
}
