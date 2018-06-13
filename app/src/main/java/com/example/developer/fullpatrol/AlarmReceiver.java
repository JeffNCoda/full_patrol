package com.example.developer.fullpatrol;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver{
    //timer values



    @Override
    public void onReceive(Context context, Intent intent){

        Intent counterIntent = new Intent();
        counterIntent.setAction("service.to.activity.transfer");
        counterIntent.putExtra("counter", "i love life");

        context.sendBroadcast(counterIntent);
        Toast.makeText(context, "We are on time", Toast.LENGTH_LONG).show();


        Intent i = new Intent();
        i.setClassName("com.example.developer.fullpatrol", "com.example.developer.fullpatrol.Display");

        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);


    }

    //timer code


}
