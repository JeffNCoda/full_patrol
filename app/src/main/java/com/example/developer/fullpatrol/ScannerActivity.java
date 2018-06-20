package com.example.developer.fullpatrol;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity  extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;
    private Calendar calendar;
    private BroadcastReceiver broadcastReceiver;
    public static final String TAG = "FIRESTORE";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Popup var
    Dialog epicDialog;
    Button positivePopupBtn, negativePopupBtn, btnAccept, btnRetry;
    TextView titleTv, messageTv;
    ImageView closePopupPositiveImg, closePopupNegativeImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Display.BROD_KILL_SCANNER)){
                    ScannerActivity.this.scannerView.stopCamera();
                    ScannerActivity.this.finish();
                }
            }
        };
        IntentFilter filter = new IntentFilter(Display.BROD_KILL_SCANNER);
        this.registerReceiver(broadcastReceiver, filter);


        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);

        scannerView = new ZXingScannerView(this);
        scannerView.setFlash(!scannerView.getFlash());
        contentFrame.addView(scannerView);

        //Popup vars initialization
        epicDialog = new Dialog(this);




    }


    public void showNegativePopup() {

        epicDialog.setContentView(R.layout.epic_popup_negative);
        closePopupNegativeImg = (ImageView)epicDialog.findViewById(R.id.closePopupNegativeImg);
        btnRetry = (Button) epicDialog.findViewById(R.id.btnRetry);
        closePopupNegativeImg.setImageResource(R.drawable.img_x);
        titleTv = (TextView)epicDialog.findViewById(R.id.titleTv);
        messageTv = (TextView)epicDialog.findViewById(R.id.messageTv);

        closePopupNegativeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
                finish();
            }
        });
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();

    }

    public void showPositivePopup() {
        epicDialog.setContentView(R.layout.epic_popup_positive);
        closePopupPositiveImg = (ImageView)epicDialog.findViewById(R.id.closePopupPositiveImg);
        closePopupPositiveImg.setImageResource(R.drawable.img_x);
        titleTv = (TextView)epicDialog.findViewById(R.id.titleTv);
        messageTv = (TextView)epicDialog.findViewById(R.id.messageTv);
        btnAccept = (Button) epicDialog.findViewById(R.id.btnAccept);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
                finish();
            }
        });

        closePopupPositiveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
                finish();
            }
        });

        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();
    }
    @Override
    public void onBackPressed(){
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        scannerView.stopCamera();
        if(this.broadcastReceiver != null){
            this.unregisterReceiver(this.broadcastReceiver);
        }
        super.onPause();
    }


    @Override
    public void handleResult(Result rawResult) {
        final String scan_Result = rawResult.getText();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calendar = Calendar.getInstance();
        String currentDateTimeString = formatter.format(calendar.getTime());
        //Call back data to main activity
        final Intent intent = new Intent(this, Display.class);
        intent.putExtra(Display.FORMAT, rawResult.getBarcodeFormat().toString());
        intent.putExtra(Display.CONTENT, rawResult.getText());
        intent.putExtra(Display.TIMESTAMP, currentDateTimeString);


        if(scan_Result != null){
            showPositivePopup();
            setResult(Activity.RESULT_OK, intent);

        }else{
            showNegativePopup();
           // finish();
        }

    }


}
