package com.example.sbuhack;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;

        LatLng Stony = new LatLng(40.91429, -73.11619);
        LatLng ny = new LatLng(40.730673, -74.002053);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Stony);
        markerOptions.title("StonyBrook");
        markerOptions.snippet("SBU HACK STARTS");
        mMap.addMarker(markerOptions);

        MarkerOptions mo = new MarkerOptions();
        mo.position(ny);
        mo.title("ny");
        mo.snippet("ny");
        mMap.addMarker(mo);

        double heading = SphericalUtil.computeHeading(Stony, ny);
        System.out.println("@@@@@@@@@@@@@@@@ "+heading);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(Stony));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }
}
