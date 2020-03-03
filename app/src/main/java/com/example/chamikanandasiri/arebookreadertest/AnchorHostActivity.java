package com.example.chamikanandasiri.arebookreadertest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.Objects;

public class AnchorHostActivity extends AppCompatActivity {

    Button hostButton;
    private Anchor anchor;
    private CustomHostARFragment arFragment;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private enum AppAnchorState {
        NONE,
        HOSTING,
        HOSTED
    }

    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    private boolean isPlaced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("Test", "initial");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anchor_host);

        arFragment = (CustomHostARFragment) getSupportFragmentManager().findFragmentById(R.id.ARFragment);
        hostButton = findViewById(R.id.HostButton);

        Log.d("Test", "Refs ready");
        prefs = getSharedPreferences("Anchor Id",MODE_PRIVATE);
        editor = prefs.edit();

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Log.d("Test", "tap came");
            if (!isPlaced) {
                Log.d("Test", "tap is not placed");
                anchor = Objects.requireNonNull(arFragment.getArSceneView().getSession()).hostCloudAnchor(hitResult.createAnchor());
                Log.d("Test", "anchor created");
                appAnchorState = AppAnchorState.HOSTING;
                showToast("hosting...");
                createModel(anchor);
                Log.d("Test", "model created");
                isPlaced = true;
            }
        });

        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (appAnchorState != AppAnchorState.HOSTING) {
                return;
            }
            Anchor.CloudAnchorState cloudAnchorState = anchor.getCloudAnchorState();
            if (cloudAnchorState.isError()) {
                showToast(cloudAnchorState.toString());
            } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {
                appAnchorState = AppAnchorState.HOSTED;
                String anchorID = anchor.getCloudAnchorId();
                editor.putString("anchorID",anchorID);
                editor.apply();
                showToast("anchor hosted successfully. AnchorId = " + anchorID);
            }
            hostButton.setOnClickListener(v -> Retrieve());
        });
    }

    private void showToast(String s) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    private void createModel (Anchor anchor){
        Log.d("Test", "Create Model");
        ModelRenderable.builder()
                .setSource(this, Uri.parse("box.sfb"))
                .build()
                .thenAccept(modelRenderable -> placeModel(anchor, modelRenderable));
    }

    private void placeModel (Anchor anchor, ModelRenderable modelRenderable){
        Log.d("Test", "Place Model");
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        isPlaced = false;
    }
    private void Retrieve () {
        Log.d("Test", "button clicked");
        String anchorID = prefs.getString("anchorID","null");
        Log.d("Test", "anchor got " + anchorID);
        if(anchorID.equals("null")){
            showToast("No Anchor ID found");
            return;
        }
        Anchor resolvedAnchor = Objects.requireNonNull(arFragment.getArSceneView().getSession()).resolveCloudAnchor(anchorID);
        createModel(resolvedAnchor);
    }
}
