package com.example.sbuhack;

import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;

public class WolfieARNaviActivity extends AppCompatActivity {
    private static final int RC_PERMISSIONS = 0x123;
    private boolean installRequested;

    private GestureDetector gestureDetector;
    private Snackbar loadingMessageSnackbar = null;

    private ArSceneView arSceneView;

    private ModelRenderable wolfieRenderable;
    private ViewRenderable chatRenderable;

    private Node wolfieNode;

    private boolean hasFinishedLoading = false; // scene loaded
    private boolean hasPlacedScene = false; // scene placed
    private boolean hasPlacedAnchor = false; // target anchor placed

    // for current degree (orientation)
    private SensorManager sensorManager;
    private SensorEventListener oriListener;

    private MapUtils mapUtils;
    Vector3 wolfieDir = new Vector3(0,0,0);

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!DemoUtils.checkIsSupportedDeviceOrFinish(this)) {
            // Not a supported device.
            return;
        }

        setContentView(R.layout.activity_ar);
        arSceneView = findViewById(R.id.ar_scene_view);

        // Build all the planet models.
        CompletableFuture<ModelRenderable> wolfieBot =
                ModelRenderable.builder().setSource(this, Uri.parse("seawolf3000(front).sfb")).build();

        // Build a renderable from a 2D View.
        CompletableFuture<ViewRenderable> chatControlBox =
                ViewRenderable.builder().setView(this, R.layout.chat_controls).build();

        CompletableFuture.allOf(
                wolfieBot,
                chatControlBox)
                .handle(
                        (notUsed, throwable) -> {
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                DemoUtils.displayError(this, "Unable to load renderable", throwable);
                                return null;
                            }

                            try {
                                wolfieRenderable = wolfieBot.get();
                                chatRenderable = chatControlBox.get();

                                // Everything finished loading successfully.
                                hasFinishedLoading = true;

                            } catch (InterruptedException | ExecutionException ex) {
                                DemoUtils.displayError(this, "Unable to load renderable", ex);
                            }

                            return null;
                        });

        // Set up a tap gesture detector.
        gestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                onSingleTap(e);
                                return true;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });

        ////////////////////////// TOUCH LISTENER //////////////////////////////
//        arSceneView
//                .getScene()
//                .setOnTouchListener(
//                        (HitTestResult hitTestResult, MotionEvent event) -> {
//                            // If the solar system hasn't been placed yet, detect a tap and then check to see if
//                            // the tap occurred on an ARCore plane to place the solar system.
//                            if (!hasPlacedScene) {
//                                return gestureDetector.onTouchEvent(event);
//                            }
//
//                            // Otherwise return false so that the touch event can propagate to the scene.
//                            return false;
//                        });

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            if (loadingMessageSnackbar == null) {
                                return;
                            }

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                if (plane.getTrackingState() == TrackingState.TRACKING) {
                                    hideLoadingMessage();
                                }
                            }
                        });

        // Lastly request CAMERA permission which is required by ARCore.
        DemoUtils.requestCameraPermission(this, RC_PERMISSIONS);

        // add onUpdate
        arSceneView.getScene().addOnUpdateListener(this::onUpdate);

        // initialize sensor manager and orientation listener
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        oriListener = new OrientationListener();

        mapUtils = new MapUtils(this);
    }

    /*
     * Place an Anchor when plane updated
     */
    private void onUpdate(FrameTime frameTime) {
        Frame frame = arSceneView.getArFrame();
        Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
        for (Plane plane : planes) {
            if (plane.getTrackingState() == TrackingState.TRACKING && !hasPlacedAnchor) {
                Anchor anchor = plane.createAnchor(plane.getCenterPose());
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arSceneView.getScene());

                if (wolfieNode == null)
                    wolfieNode = createWolfie();
                anchorNode.addChild(wolfieNode);

                hasPlacedAnchor = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSceneView == null) {
            return;
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = DemoUtils.hasCameraPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            showLoadingMessage();
        }

        // for orientation
        sensorManager.registerListener(oriListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);

        mapUtils.init();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapUtils.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (arSceneView != null) {
            arSceneView.pause();
        }

        sensorManager.unregisterListener(oriListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (arSceneView != null) {
            arSceneView.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!DemoUtils.hasCameraPermission(this)) {
            if (!DemoUtils.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                DemoUtils.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onSingleTap(MotionEvent tap) {
        if (!hasFinishedLoading) {
            // We can't do anything yet.
            return;
        }

        Frame frame = arSceneView.getArFrame();
        if (frame != null) {
            if (!hasPlacedScene && tryPlace(tap, frame)) {
                hasPlacedScene = true;
            }
        }
    }

    private boolean tryPlace(MotionEvent tap, Frame frame) {
        if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    // Create the Anchor.
                    Anchor anchor = hit.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arSceneView.getScene());
                    wolfieNode = createWolfie();
                    anchorNode.addChild(wolfieNode);
                    return true;
                }
            }
        }

        return false;
    }

    private Node createWolfie() {
        LatLng ny = new LatLng(40.730673, -74.002053);
        mapUtils.setTargetPosition(ny);

        Node base = new Node();
        Vector3 vectorValue = new Vector3(0,0,0);
        base.setLookDirection(new Vector3(0.0f, 0.0f, 0.0f));

        Node wolfie = new Node();
        wolfie.setParent(base);
        wolfie.setLocalPosition(new Vector3(0.0f, 0.0f, 0.0f));
//        base.setLocalScale(new Vector3(3f, 3f, 3f));

        Node wolfieVisual = new Node();
        wolfieVisual.setParent(wolfie);
        wolfieVisual.setRenderable(wolfieRenderable);
        wolfieVisual.setLocalScale(new Vector3(20f, 20f, 20f));

        Node chatControls = new Node();
        chatControls.setParent(base);
        chatControls.setRenderable(chatRenderable);
        chatControls.setLocalPosition(new Vector3(0.5f, 0.25f, 0.0f));

        View solarControlsView = chatRenderable.getView();

        Button sacButton = solarControlsView.findViewById(R.id.sacButton);
        TextView fText = solarControlsView.findViewById(R.id.Fuck);
        TextView fText1 = solarControlsView.findViewById(R.id.orbitHeader);
        TextView fText3 = solarControlsView.findViewById(R.id.rotationHeader);
        sacButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                float oriDegree = ((OrientationListener)oriListener).getDegree();
                float targetDegree = (float) mapUtils.getHeading();

                float rotate = targetDegree - oriDegree + 90;

                Toast.makeText(getApplicationContext(),"ori : " + oriDegree + "\ntar : " +targetDegree +"\nrot : " + rotate,Toast.LENGTH_LONG).show();

                Quaternion rotation = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), -rotate);

                Node parent = wolfieNode.getParent();
                wolfieNode.setParent(null);
                wolfieNode = createWolfie();
                wolfieNode.setParent(parent);
                wolfieNode.setLocalRotation(rotation);
                /*wolfieDir.set((wolfieDir.x + 0.1f), wolfieDir.y, wolfieDir.z);
                Toast.makeText(getApplicationContext(),wolfieDir.x +"is the value and the value is " +
                        wolfieNode.getLocalScale().x + "and " + wolfieNode.getLocalScale().y + "and " +
                        wolfieNode.getLocalScale().z , Toast.LENGTH_LONG).show();
                wolfieNode.setLookDirection(wolfieDir);*/
                if(fText.getText() != null){
//                    sacButton.setVisibility(View.INVISIBLE);
//                    fuckText1.setVisibility(View.INVISIBLE);
//                    fuckText2.setVisibility(View.INVISIBLE);
//                    fuckText3.setVisibility(View.INVISIBLE);
//                    fuckText.setVisibility(View.VISIBLE);
                }
            }
        });

        // Toggle the solar controls on and off by tapping the sun.
        wolfieVisual.setOnTapListener(
                (hitTestResult, motionEvent) -> chatControls.setEnabled(!chatControls.isEnabled()));

        return base;
    }


    private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

        loadingMessageSnackbar =
                Snackbar.make(
                        WolfieARNaviActivity.this.findViewById(android.R.id.content),
                        R.string.plane_finding,
                        Snackbar.LENGTH_INDEFINITE);
        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        loadingMessageSnackbar.show();
    }

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }
}