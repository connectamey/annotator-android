package com.example.speechrecognizer;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.speechrecognizer.Database.AppDatabase;
import com.example.speechrecognizer.Database.User;
import com.example.speechrecognizer.Database.UserDao;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    public static final int PERMISSION_RESULT_CODE = 999;
    private ImageButton button;
    private Button startServiceButton, stopServiceButton;
    private EditText text;
    public UserDao userDao;
    public User user;
    public List<User> users, usersListToShow;
    public AppDatabase db;
    public Runnable runnable;
    public HashMap<String, String> hashMap;
    Intent SpeechRecognizerServiceIntent;


    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        startServiceButton = findViewById(R.id.startservicebutton);
        stopServiceButton = findViewById(R.id.stopservicebutton);
        text = findViewById(R.id.text);
        SpeechRecognizerServiceIntent = new Intent(getApplicationContext(), SpeechRecognizerService.class);

//        button.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            public void onClick(View v) {
//                if (checkPermission()) {
//                    StartSpeechRecognition();
//                } else {
//                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
//                            PERMISSION_RESULT_CODE);
//                }
//
//            }
//        });

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                {
                    if (checkPermission()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getApplicationContext().startForegroundService(SpeechRecognizerServiceIntent);
                        } else {
                            startService(SpeechRecognizerServiceIntent);
                        }
//                StartSpeechRecognition();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                PERMISSION_RESULT_CODE);
                    }
                }
            }
        });
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpeechRecognizerService.TIMER_STATUS = false;
                SpeechRecognizerService.speechRecognizer.stopListening();
                SpeechRecognizerService.speechRecognizer.cancel();
                SpeechRecognizerService.speechRecognizer.destroy();
                stopService(SpeechRecognizerServiceIntent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        db = Room.databaseBuilder(getApplicationContext(),
                                AppDatabase.class, "database-name").allowMainThreadQueries().build();
                        user = new User();
                        userDao = db.userDao();
                        users = userDao.getAll();
                        usersListToShow = users;
                        if (usersListToShow.size() > 2){
                        text.setText(usersListToShow.get(usersListToShow.size()-1).timestamp
                                + "\t" + usersListToShow.get(usersListToShow.size()-1).activity + "\n \n"
                                + usersListToShow.get(usersListToShow.size()-2).timestamp
                                + "\t" + usersListToShow.get(usersListToShow.size()-2).activity + "\n \n"
                                + usersListToShow.get(usersListToShow.size()-3).timestamp
                                + "\t" + usersListToShow.get(usersListToShow.size()-3).activity + "\n \n");
                        }
                        else
                        {
                            text.setText("Latest 3 entries will be shown. Talk for 30 seconds first.\n \n" +
                                    "Each entry will be of 10 seconds.");
                        }
                    }
                };
                runOnUiThread(runnable);
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
                    Intent SpeechRecognizerServiceIntent = new Intent(getApplicationContext(), SpeechRecognizerService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getApplicationContext().startForegroundService(SpeechRecognizerServiceIntent);
                    } else {
                        startService(SpeechRecognizerServiceIntent);
                    }
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
            public void onEndOfSpeech() {
                Log.d("TAG", "onEndOfSpeech: ");
            }

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
//                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
//                text.setText(results.get(0));
                Log.d("words", results.get(0));
            }

            @Override
            public void onRmsChanged(float v) {}

        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer.startListening(intent);

//        Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
    }

}