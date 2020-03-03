package com.example.chamikanandasiri.arebookreadertest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.example.chamikanandasiri.arebookreadertest.CustomARFragment;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.Collection;

public class ImageOverlayActivity extends AppCompatActivity implements Scene.OnUpdateListener {

    CustomARFragment customARFragment;
    //Bitmap kbBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_overlay);
//        kbBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kb);
//        Log.d("Test", "bitmap created");
        customARFragment = (CustomARFragment) getSupportFragmentManager().findFragmentById(R.id.ARFragment);
        assert customARFragment != null;
        customARFragment.getArSceneView().getScene().addOnUpdateListener(this);
    }

    public void setupDatabase(Config config, Session session) {
        Bitmap kbBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dolphin);
        Log.d("Test", "db inside");
        AugmentedImageDatabase database = new AugmentedImageDatabase(session);
        Log.d("Test", "db inside2");
        if (kbBitmap != null) {
            database.addImage("kb", kbBitmap);
            Log.d("Test", "db added");
        }
        config.setAugmentedImageDatabase(database);
        Log.d("Test", "db set up");
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        Log.d("Test", "update called");
        Frame frame = customARFragment.getArSceneView().getArFrame();
        assert frame != null;
        Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);
        Log.d("Test", "update called 23");

        for (AugmentedImage image : images) {
            Log.d("Test", "first image");
            if (image.getTrackingState() == TrackingState.TRACKING)
                Log.d("Test", "Tracking");
                if (image.getName().equals("dolphin")) {
                    Log.d("Test", "Got dolphin");
                    Anchor anchor = image.createAnchor(image.getCenterPose());
                    Log.d("Test", "Create Anchor");
                    createModel(anchor);
                }
        }
    }

    private void createModel(Anchor anchor) {
        Log.d("Test", "Create Model");
        ModelRenderable.builder()
                .setSource(this, Uri.parse("box.sfb"))
                .build()
                .thenAccept(modelRenderable -> placeModel(anchor, modelRenderable));
    }

    private void placeModel(Anchor anchor, ModelRenderable modelRenderable) {
        Log.d("Test", "Place Model");
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(modelRenderable);
        customARFragment.getArSceneView().getScene().addChild(anchorNode);
    }
}
