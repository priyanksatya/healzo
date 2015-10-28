package com.healzo.doc;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyStartupIntentReceiver extends BroadcastReceiver{
@Override
public void onReceive(Context context, Intent intent) {
Intent myIntent = new Intent(context, RequestReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, 0);
    AlarmManager alarmManager = (AlarmManager) context .getSystemService(Context.ALARM_SERVICE);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),30*60000, pendingIntent);
}
}