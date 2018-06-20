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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver{
    public static final String ACTION_KILL_ALARM = "broadcast.kill.Alarm";
    public static final String ACTION_REST_COUNTER = "broadcast.reset.counter";
    //timer values




    @Override
    public void onReceive(Context context, Intent intent){

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.getTime().getHours(), min = calendar.getTime().getMinutes();
        int endHour = intent.getIntExtra(MainActivity.EXTRA_END_HOUR, 0), endMin = intent.getIntExtra(MainActivity.EXTRA_END_MIN, 0);
        if(hour >= endHour && min >= endMin){
            //Session Over
            Intent alarmKiller = new Intent();
            alarmKiller.setAction(ACTION_KILL_ALARM);
            context.sendBroadcast(alarmKiller);
        }else{
            startPatrol(context);
            Intent resetCounter = new Intent();
            resetCounter.setAction(ACTION_REST_COUNTER);
            context.sendBroadcast(resetCounter);
        }


    }

    public void startPatrol(Context context){



        Intent i = new Intent(context, Display.class);

        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
    //timer code


}
