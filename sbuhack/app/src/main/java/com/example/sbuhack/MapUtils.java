package com.example.sbuhack;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MapUtils {
    private Context ctx;

    private static final int UPDATE_INTERVAL_MS = 1000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    Location mCurrentLocatiion;
    LatLng currentPosition;
    LatLng targetPosition;

    protected static final LatLng[] SBU_TARGET_POSITIONS = {
            new LatLng(40.914297, -73.123638), //SAC
            new LatLng(40.909657, -73.116138), //Stony Brook Hospital
            new LatLng(40.921307, -73.127839)  //Stony Brook LIRR Station
    };

    private boolean isPositionSet = false;
    String[] REQUIRED_PERMISSIONS  = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    public MapUtils(Context ctx) {
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        this.ctx = ctx;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);

        targetPosition = SBU_TARGET_POSITIONS[2];
    }

    public void init() {
        initLocationUpdates();
        if (checkPermission()) {
            start();
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locations = locationResult.getLocations();
            if (locations.size() > 0) {
                location = locations.get(locations.size() - 1);
                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());
                mCurrentLocatiion = location;

                isPositionSet = true;
            }
        }
    };

    private void initLocationUpdates() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions( (Activity)ctx, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE);
            return;
        }

        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    protected void start() {
        if (checkPermission()) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    protected void stop() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private boolean checkPermission() {

        int hasFinePermission = ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarsePermission = ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFinePermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarsePermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }
        return false;
    }

    public boolean isPositionSet() {
        return isPositionSet;
    }

    public void setTargetPosition(LatLng position) {
        targetPosition = position;
    }

    public double getHeading() {
/*        LatLng Stony = new LatLng(40.91429, -73.11619);
        LatLng ny = new LatLng(40.730673, -74.002053);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Stony);
        markerOptions.title("StonyBrook");
        markerOptions.snippet("SBU HACK STARTS");

        MarkerOptions mo = new MarkerOptions();
        mo.position(ny);
        mo.title("ny");
        mo.snippet("ny");*/

        // convert (-pi ~ +pi) to (0 ~ 2pi)
        double heading = SphericalUtil.computeHeading(currentPosition, targetPosition);
        if (heading < 0)
            heading = 360 + heading;
        return heading;
    }
}
