package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent arActivityIntent = new Intent(this, ARActivity.class);
        Button startARActivityBt = (Button) findViewById(R.id.arButton);
        startARActivityBt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(arActivityIntent);
            }
        });

        final Intent googleMapActivityIntent = new Intent(this, GoogleMapActivity.class);
        Button startGoogleMapBt = (Button) findViewById(R.id.googleMapButton);
        startGoogleMapBt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { startActivity(googleMapActivityIntent);}
        });



    }
}
