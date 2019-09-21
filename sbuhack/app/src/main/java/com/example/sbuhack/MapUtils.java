package com.example.sbuhack;

import android.Manifest;
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

    private boolean isPositionSet = false;

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
    }

    public void onMapReady() {
        if (checkPermission()) {
            startLocationUpdates();
        }
        start();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());
                mCurrentLocatiion = location;

                isPositionSet = true;
            }
        }
    };

    private void startLocationUpdates() {
        if (!checkPermission()) {
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
        int hasCoarsPermission = ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFinePermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarsPermission == PackageManager.PERMISSION_GRANTED   ) {
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
