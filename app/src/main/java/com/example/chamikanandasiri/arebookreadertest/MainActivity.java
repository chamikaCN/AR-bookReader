package com.example.chamikanandasiri.arebookreadertest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    ArFragment arFragment;
    Button textRecognizerButton, imageOverlayButton, corousalButton, hostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textRecognizerButton = findViewById(R.id.textRecognizerButton);
        imageOverlayButton = findViewById(R.id.overlayButton);
        corousalButton = findViewById(R.id.corousalButton);
        hostButton = findViewById(R.id.anchorHostButton);
        textRecognizerButton.setOnClickListener(v -> changeActivity(1));
        imageOverlayButton.setOnClickListener(v -> changeActivity(2));
        corousalButton.setOnClickListener(v -> changeActivity(3));
        hostButton.setOnClickListener(v -> changeActivity(4));
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();

            ModelRenderable.builder().setSource(this, Uri.parse("box.sfb")).build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage()).show();
                        return null;
                    });
        });
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

    public void changeTheModel(View view) {
        Button button = findViewById(R.id.textRecognizerButton);
        button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    public void changeActivity(int activityNumber) {
        Intent intent;
        if (activityNumber == 1) {
            intent = new Intent(this, TextDetectionActivity.class);
        } else if (activityNumber == 3) {
            intent = new Intent(this, CorousalActivity.class);
        }else if (activityNumber == 4) {
            intent = new Intent(this, AnchorHostActivity.class);
        }else {
            intent = new Intent(this, ImageOverlayActivity.class);
        }
        startActivity(intent);
    }
}
