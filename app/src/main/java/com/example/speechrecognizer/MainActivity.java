package com.example.speechrecognizer;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final int PERMISSION_RESULT_CODE = 999;
    private ImageButton button;
    private EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (ImageButton) findViewById(R.id.button);
        text = (EditText) findViewById(R.id.text);

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onClick(View v) {
                if (checkPermission()) {
                    StartSpeechRecognition();
                } else {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSION_RESULT_CODE);
                }

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermission()
    {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_RESULT_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void StartSpeechRecognition(){
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onBufferReceived(byte[] bytes) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int i) {
                String msg = "ERROR";
                switch (i) {
                    case SpeechRecognizer.ERROR_AUDIO: msg = "ERROR_AUDIO"; break;
                    case SpeechRecognizer.ERROR_CLIENT: msg = "ERROR_CLIENT"; break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: msg = "ERROR_INSUFFICIENT_PERMISSIONS"; break;
                    case SpeechRecognizer.ERROR_NETWORK: msg = "ERROR_NETWORK"; break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: msg = "ERROR_NETWORK_TIMEOUT"; break;
                    case SpeechRecognizer.ERROR_NO_MATCH: msg = "ERROR_NO_MATCH"; break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: msg = "ERROR_RECOGNIZER_BUSY"; break;
                    case SpeechRecognizer.ERROR_SERVER: msg = "ERROR_SERVER"; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: msg = "ERROR_SPEECH_TIMEOUT"; break;
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEvent(int i, Bundle bundle) {}

            @Override
            public void onPartialResults(Bundle bundle) {}

            @Override
            public void onReadyForSpeech(Bundle bundle) {}

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> results =
                        bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                text.setText(results.get(0));
            }

            @Override
            public void onRmsChanged(float v) {}

        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer.startListening(intent);

        Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
    }

}