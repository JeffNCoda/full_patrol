package com.example.developer.fullpatrol;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //setup data
    public static final String TAG = "uploading stuff";
    private int intervalTimer;
    private int patrolTimer;
    private int countDown;
    private String[] pointCollection;
    private Map<String, Object> setUp = new HashMap<>();
    //firebase stuff
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("site").document("jfGtB20apFAHdEYsgb8H");

    private TextView timedOut;
    private TextView textView;
    //timer values

    private static final long START_TIME_IN_MILLIS = 15*60000;
    private TextView mTextViewCountDown;
    private CountDownTimer mCountDownTimer;

    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;

    //alarm
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    //broadcastreceiver intenter
    Context context;
    BroadcastReceiver updateUIReciver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.display);
        mTextViewCountDown = (TextView) findViewById(R.id.mTextViewCountDown);
        timedOut = (TextView) findViewById(R.id.intervalTimer);






        updateCountDownText();

        //timer
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm to start at 8:30 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 1);

        // setRepeating() lets you specify a precise custom interval--in this case,
        // 20 minutes.
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000*60*15, alarmIntent);

        //intent to display time
        context = this;

        IntentFilter filter = new IntentFilter();
        filter.addAction("service.to.activity.transfer");
        updateUIReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                if (intent != null){
                    //Toast.makeText(context,, Toast.LENGTH_LONG).show();
                    timedOut.setText( intent.getStringExtra("counter").toString());
                }
            }
        };
        registerReceiver(updateUIReciver, filter);
    }
    public void getPatrolData(View view){
        //getSet upData
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        setUp = document.getData();
                        //dirty code to get points into array
                        pointCollection = setUp.get("pointCollection").toString().replaceAll("\\[|\\]|\\s", "").split(",");
                        intervalTimer = document.getLong("intervalTimer").intValue();
                        countDown = document.getLong("countDown").intValue();
                        patrolTimer = document.getLong("patrolTimer").intValue();
                       //for debugging purposes
                        textView.setText(pointCollection[4] +"\npatrolTimer :"+ Integer.toString(patrolTimer*60) + "\ncountDown :" +Integer.toString(countDown*60));

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //timer code

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;

                updateCountDownText();
                if (millisUntilFinished ==0 ){
                    mTimeLeftInMillis = START_TIME_IN_MILLIS;
                }

            }

            @Override
            public void onFinish() {

            }
        }.start();


    }
    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText("Time remaining: "+timeLeftFormatted);
    }


}
