package com.example.developer.fullpatrol;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Display extends AppCompatActivity {
    private TextView view;


    private static final long START_TIME_IN_MILLIS = 7*60000;
    private TextView mTextViewCountDown;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private Button startPatrol;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;

    public String timeLeftFormatted;
    public ListView listview;

    ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    //RECORDING HOW MANY TIMES THE BUTTON HAS BEEN CLICKED

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button btnScan;


    //stuff from main
    private final static int REQUEST_SCANNER = 1;
    public final static String FORMAT = "format";
    public final static String CONTENT = "content";
    public final static String LOCATION = "location";
    public final static String TIMESTAMP = "time";
    private static final String TAG = "Firestore--you" ;
    TextView timedOut;
    TextView textView;
    private String[] pointCol;

    private DocumentReference docRef = db.collection("site").document("jfGtB20apFAHdEYsgb8H");
    private Map<String, Object> setUp = new HashMap<>();
    private int intervalTimer;
    private int patrolTimer;
    private int countDown;
    Button store_data;
    Context context;
    boolean complete, started;
    private boolean patrolStarted;
    private String collection;
    private static final int MIN_TO_MIL = 60000;
    private int count;
    private CountDownTimer countDownOut;
    private boolean timedOutBeforeStart = false;
    private TextView remainingPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        patrolStarted = false;
        collection = "patrolDataDummy";
        context =this;
        count = 5;
        started = false;
        complete = false;
        getPatrolData();


        //START_TIME_IN_MILLIS = patrolTimer*60000;
       //


        listview = (ListView) findViewById(R.id.listview);

        store_data = (Button)findViewById(R.id.store_data);
        remainingPoints = (TextView)findViewById(R.id.remainingPoints);
        btnScan = findViewById(R.id.btn_scan);


        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        timedOut = findViewById(R.id.timeOut);
        textView = findViewById(R.id.textView);


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Display.this, ScannerActivity.class);
                startActivityForResult(intent, REQUEST_SCANNER);
            }
        });


        displayMissedPoints(listItems);
        view = findViewById(R.id.view_content);
       //startTimer();
       //startTimerStop(countDown);

    }
    public void checkStartPatrol(){
        if(listItems.contains(pointCol[0]) && listItems.size() == 1){
            started = true;
            sendEventType(db, collection, "Patrol started", 3,"");
            Toast.makeText(getApplicationContext(), "Patrol started", Toast.LENGTH_LONG).show();
        }else if(listItems.size() == 1){

            Toast.makeText(context, " Not starting point ", Toast.LENGTH_LONG).show();
            listItems.clear(); //empty non start points
        }
    }
    public void patrol(){
        //check if patrol started
        checkStartPatrol();
        //check if patrol ended
        if(listItems.size() > 1){
            if (listItems.get(listItems.size()-1).equals(pointCol[0])){
                Toast.makeText(context, " --Patrol ended-- ", Toast.LENGTH_LONG).show();
                verityPatrol(listItems, pointCol);
            }
        }
    }

    public void pressPanic(View v){
        count--;
        Log.d(TAG, "pressed Panic "+Integer.toString(count));
        if(count==0){
            sendEventType(db,collection,"Panic", 8, "");
            count = 5;
        }else{
            Toast.makeText(getApplicationContext(), String.format("press panic %d more times",count), Toast.LENGTH_LONG).show();
        }
    }
    public void getPatrolData(){
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
                        pointCol = setUp.get("pointCollection").toString().replaceAll("\\[|\\]|\\s", "").split(",");
                        intervalTimer = document.getLong("intervalTimer").intValue();
                        countDown = document.getLong("countDown").intValue()* MIN_TO_MIL;
                        patrolTimer = document.getLong("patrolTimer").intValue()* MIN_TO_MIL;
                        mTimeLeftInMillis = patrolTimer;
                        startTimer();
                        startTimerStop(countDown);
                        //for debugging purposes
                        textView.setText(pointCol[4] +"\npatrolTimer :"+ Integer.toString(patrolTimer*60) + "\ncountDown :" +Integer.toString(countDown*60));

                    } else {
                        Log.d(TAG, "No such document");
                        //load from offline data
                        String source = document.getMetadata().isFromCache() ?
                                "local cache" : "server";
                        Log.d(TAG, "Data fetched from " + source);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    public void displayMissedPoints(ArrayList listItems){
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listview.setAdapter(adapter);
    }
    public void sendEventType(FirebaseFirestore db, String collection, String description, int eventID, String siteID){
        Map<String, Object> event = new HashMap<>();
        event.put("siteId", siteID);
        event.put("eventId", eventID);
        event.put("description", description);
        event.put("timeStamp", FieldValue.serverTimestamp());
        event.put("location", "N-S");
        addSite(db, collection, event);

    }
    public void sendEventType(FirebaseFirestore db, String collection, String description, String pointId, int eventID, String siteID){
        Map<String, Object> event = new HashMap<>();
        event.put("siteId", siteID);
        event.put("eventId", eventID);
        event.put("pointId", pointId);
        event.put("description", description);
        event.put("timeStamp", FieldValue.serverTimestamp());
        event.put("location", "N-S");
        addSite(db, collection, event);

    }
    public void addSite(FirebaseFirestore db, String collectionName, Map object){

        db.collection(collectionName).add(object).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "DocumentSnapshot added with ID" + documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });

    }
    private void verifyPatrolVisually(ArrayList<String> scannedPoints, String[] pointCollection){

        ArrayList<String> missedPoints = new ArrayList<>();
        //point count
        int falsePoints = 0;
        //loop over scanned points and check if all
        for(int i=0; i < pointCollection.length;i++){
            if(!scannedPoints.contains(pointCollection[i])){
                //add this
                falsePoints++;
                missedPoints.add(pointCollection[i]);

            }else if(mTimeLeftInMillis <= 1990){
                timedOut.setText("timed out: "+ String.valueOf(mTimeLeftInMillis));

            }
        }
        int tot =  missedPoints.size();
        remainingPoints.setText("Remaining points "+Integer.toString(tot));

        if(tot>0){
            String allPoints ="\tMissed Points\n";
            //display missed points
            for(int i =0; i< missedPoints.size(); i++){
                allPoints += missedPoints.get(i)+"\n";
            }
            textView.setText(allPoints);

        }else{
            String allPoints ="All points scanned!\n";
            textView.setText(allPoints);
        }
        if(tot==0 && mTimeLeftInMillis >= 1990 && listItems.get(listItems.size()-1).equals(pointCol[0])){

            Toast.makeText(context, "Good Patrol "+listItems.get(listItems.size()-1).equals(pointCol[0]), Toast.LENGTH_LONG).show();
        }

    }
    private void verityPatrol(ArrayList<String> scannedPoints, String[] pointCollection) {
        //verification started var
        complete = true;
        //array of missed points
        ArrayList<String> missedPoints = new ArrayList<>();

        //loop over scanned points and check if all
        for(int i=0; i < pointCollection.length;i++){
            if(!scannedPoints.contains(pointCollection[i])){
                //add this
                missedPoints.add(pointCollection[i]);

            }//else if(mTimeLeftInMillis <= 1990){
                //timedOut.setText("timed out: "+ String.valueOf(mTimeLeftInMillis));
           // }
        }
        int tot =  missedPoints.size();
        //timedOut.setText("Remaining points "+Integer.toString(tot));

        if(tot>0){
            //String allPoints ="Missed Points\n";
            //display missed points
           // for(int i =0; i< missedPoints.size(); i++){
             //   allPoints += missedPoints.get(i)+"\n";
           // }
            //textView.setText(allPoints);
            //if(complete){
                //send missed point eventType 4
            sendEventType(db,collection,"Missed point(s)", 4, "");
            Toast.makeText(getApplicationContext(), String.format("%d points missing",tot), Toast.LENGTH_LONG).show();
            finish();
            //}
        }//else{
            //String allPoints ="All points scanned!\n";
            //textView.setText(allPoints);
       // }
        if(tot==0 && mTimeLeftInMillis >= 1990 && listItems.get(listItems.size()-1).equals(pointCol[0])){
            //send good patrol eventType 7
            sendEventType(db,collection,"Good patrol", 7, "");
            finish();

        }
    }

    private void startTimerStop(int countDowning) {
        countDownOut = new CountDownTimer(countDowning, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long mTimeLeftInMillisCountOut = millisUntilFinished;

                if(started){
                    timedOut.setText("");
                }else {


                    if (millisUntilFinished <= 1900) {
                        mTimeLeftInMillisCountOut = countDown;
                        timedOutBeforeStart = true;
                    }

                    int minutes = (int) (mTimeLeftInMillisCountOut / 1000) / 60;
                    int seconds = (int) (mTimeLeftInMillisCountOut / 1000) % 60;

                    String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                    timedOut.setText("Timeout in: \n\n  " + timeLeftFormatted);//.setText("Time remaining: \n\n  "+timeLeftFormatted +"\n"+ "");
                }

            }

            @Override
            public void onFinish() {
                mTimerRunning = false;

            }
        }.start();

        mTimerRunning = true;
    }
    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;

                if(timedOutBeforeStart && !started){
                    finish();
                }
                if (millisUntilFinished <= 2000 && started ){
                    verityPatrol(listItems, pointCol);
                    //mTimeLeftInMillis = START_TIME_IN_MILLIS;
                }

                updateCountDownText();


            }

            @Override
            public void onFinish() {
                mTimerRunning = false;

            }
        }.start();

        mTimerRunning = true;
    }
    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        view.setText("Time remaining: \n\n  "+timeLeftFormatted +"\n");
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        btnScan.setText("Start Patrol");
        startTimer();
        //startTimerStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        Bundle bundle = data.getExtras();
        //String content_ = bundle.getString(CONTENT);
        Log.d("POINTS", "scanned point "+bundle.getString(CONTENT));

        if(bundle.getString(CONTENT).equals("null")){
            Toast.makeText(getApplicationContext(),"BACK PRESSED", Toast.LENGTH_LONG).show();
        }else {
            //check if patrol started
            if(bundle.getString(CONTENT)!=null && Arrays.asList(pointCol).contains(bundle.getString(CONTENT))){
                listItems.add(bundle.getString(CONTENT));
                patrol();
                if(listItems.size()>1){
                    //create scanned point event
                    sendEventType(db,collection,"",bundle.getString(CONTENT), 0, "");
                    Toast.makeText(getApplicationContext(),"Point scanned", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(context, "Point not from site", Toast.LENGTH_LONG).show();
            }

            verifyPatrolVisually(listItems, pointCol);
            displayMissedPoints(listItems);
        }


    }

}
