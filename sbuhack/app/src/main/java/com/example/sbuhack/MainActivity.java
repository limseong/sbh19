package com.example.sbuhack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VideoView videoview = (VideoView) findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.backvideo);
        videoview.setVideoURI(uri);
        videoview.start();


        final Intent arActivityIntent = new Intent(this, WolfieARNaviActivity.class);
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
    @Override
    protected void onResume() {

        super.onResume();
        VideoView videoview = (VideoView) findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.backvideo);
        videoview.setVideoURI(uri);
        videoview.start();
    }
}
