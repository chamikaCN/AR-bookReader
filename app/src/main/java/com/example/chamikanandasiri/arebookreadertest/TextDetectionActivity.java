package com.example.chamikanandasiri.arebookreadertest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Locale;

public class TextDetectionActivity extends AppCompatActivity {

    String detectedString;
    EditText text;
    SeekBar speed,pitch;
    Button speakButton,stopButton;
    TextToSpeech speech;
    SurfaceView cameraView;
    TextView detectedText;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }try {
                        cameraSource.start(cameraView.getHolder());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_detection);

        text = findViewById(R.id.text);
        speed = findViewById(R.id.speedBar);
        pitch = findViewById(R.id.pitchBar);
        speakButton = findViewById(R.id.speakButton);
        stopButton = findViewById(R.id.stopButton);
        cameraView = findViewById(R.id.Surface);
        detectedText = findViewById(R.id.detectionLabel);

//        speakButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        TextRecognizer textRecognizer = new  TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational()){
            Log.w("TextDetectionActivity","Detector dependencies are not yet available");
        }else{
            cameraSource = new CameraSource.Builder(getApplicationContext(),textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(350,350)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(TextDetectionActivity.this,
                                    new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0){
                        detectedText.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i=0; i< items.size(); i++){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                detectedString = stringBuilder.toString();
                                detectedText.setText(detectedString);
                            }
                        });
                    }
                }
            });
        }


        speech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = speech.setLanguage(Locale.UK);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS","language not supported");
                    }else{
                        speakButton.setEnabled(true);
                    }
                }else {
                    Log.e("TTS","initializing failed");
                }
            }
        }
        );

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speech.stop();
            }
        });
    }

      void speak(){
        String textValue = text.getText().toString();
        float speedValue = (float)speed.getProgress()/50;
        if(speedValue < 0.1) speedValue = 0.1f;
        float pitchValue = (float)pitch.getProgress()/50;
        if(pitchValue < 0.1) pitchValue = 0.1f;
        speech.setPitch(pitchValue);
        speech.setSpeechRate(speedValue);
          if (detectedString != null) {
              speech.speak(detectedString,TextToSpeech.QUEUE_FLUSH,null);
          }else{
              speech.speak(textValue,TextToSpeech.QUEUE_FLUSH,null);
          }
    }


    @Override
    protected void onDestroy() {
        if(speech != null) {
            speech.stop();
            speech.shutdown();
        }
        super.onDestroy();
    }
}


