package com.example.speechrecognizer;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.example.speechrecognizer.Database.AppDatabase;
import com.example.speechrecognizer.Database.User;
import com.example.speechrecognizer.Database.UserDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class SpeechRecognizerService extends Service {
public Notification stickyServiceNotification;
public NotificationCompat.Builder notificationBuilder;
    // Constants
    private static final int ID_SERVICE = 101;
    public static ArrayList<String> results;
    public static boolean TIMER_STATUS;
    public UserDao userDao;
    public User user;
    public List<User> users;
    public AppDatabase db;
    public Runnable runnable;
    public SimpleDateFormat simpleDateFormat;
    public String currentDateandTime;
    public PendingIntent pendingIntent;
    public Intent intent;
    public TaskStackBuilder taskStackBuilder;
    public static CountDownTimer countDownTimer;
    public static SpeechRecognizer speechRecognizer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TIMER_STATUS = true;
        Log.d("TAG", "onStartCommand: ");

             countDownTimer = new CountDownTimer(20000, 10000) {
            public void onTick(long millisUntilFinished) {

                StartSpeechRecognition();
                Log.d("TIMER STATUS", "onTick: timer launched");
            }

            public void onFinish() {
                Log.d("TIMER STATUS", "onFinish: Timer Finished");
                if (TIMER_STATUS)
                start();// here, when your CountDownTimer has finished , we start it again :)
                else {
                    speechRecognizer.destroy();
                };
            }
        };
        countDownTimer.start();


        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TAG", "onBind: service ");
        return null;
    }

    @Override
    public boolean stopService(Intent name) {
        TIMER_STATUS = false;
        countDownTimer.cancel();
        stopSelf();
        return super.stopService(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // do stuff like register for BroadcastReceiver, etc.

        // Create the Foreground Service
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").build();
        user = new User();
        userDao = db.userDao();
        users = new List<User>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(@Nullable Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<User> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return null;
            }

            @Override
            public boolean add(User user) {
                return false;
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends User> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, @NonNull Collection<? extends User> c) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public User get(int index) {
                return null;
            }

            @Override
            public User set(int index, User element) {
                return null;
            }

            @Override
            public void add(int index, User element) {

            }

            @Override
            public User remove(int index) {
                return null;
            }

            @Override
            public int indexOf(@Nullable Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(@Nullable Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<User> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<User> listIterator(int index) {
                return null;
            }

            @NonNull
            @Override
            public List<User> subList(int fromIndex, int toIndex) {
                return null;
            }
        };

//        users = userDao.getAll();
        intent = new Intent(getApplicationContext(), MainActivity.class);
        taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        notificationBuilder = new NotificationCompat.Builder(this, channelId);
        stickyServiceNotification = notificationBuilder.setOngoing(true)
                .setContentTitle("Activity Tracking Active")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(ID_SERVICE, stickyServiceNotification);
//        StartSpeechRecognition();
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
    public void StartSpeechRecognition(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

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

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        results =
                                bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                text.setText(results.get(0));
                        Log.d("TIMER STATUS words:", results.get(0));
//                        user.timestamp  = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.getDefault());
                        currentDateandTime = simpleDateFormat.format(new Date());
                        user.timestamp = currentDateandTime;
                        user.activity = results.get(0);
                        users.add(user);
                        userDao.insertAll((User) user);

                        //                Log.d("TIMER STATUS ROOM", "onResults: " + userDao.getAll().toArray().toString());

                    }
                };


                AsyncTask.execute(runnable);
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