package com.example.sbuhack;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class OrientationListener implements SensorEventListener {
    float degree = 0; // 0=North, 90=East, 180=South, 270=West

    @Override
    public void onSensorChanged(SensorEvent event) {
        degree = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public float getDegree() {
        return degree;
    }
}
