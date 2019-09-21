package com.example.sbuhack;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

public class MapUtils {
    public double getHeading() {
        LatLng Stony = new LatLng(40.91429, -73.11619);
        LatLng ny = new LatLng(40.730673, -74.002053);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Stony);
        markerOptions.title("StonyBrook");
        markerOptions.snippet("SBU HACK STARTS");

        MarkerOptions mo = new MarkerOptions();
        mo.position(ny);
        mo.title("ny");
        mo.snippet("ny");

        double heading = SphericalUtil.computeHeading(Stony, ny);
        return heading;
    }

}
