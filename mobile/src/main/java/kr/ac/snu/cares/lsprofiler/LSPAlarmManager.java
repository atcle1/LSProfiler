package kr.ac.snu.cares.lsprofiler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import kr.ac.snu.cares.lsprofiler.receivers.AlarmReceiver;

/**
 * Created by summer on 4/17/15.
 */
public class LSPAlarmManager {
    public static final String TAG = LSPAlarmManager.class.getSimpleName();
    private static AlarmManager alarmManager;
    private Context context;
    private static long nextAlarmTime;
    private static PendingIntent alarmPendingIntent;
    private BroadcastReceiver alarmReceiver = new AlarmReceiver();
    private static LSPAlarmManager instance;


    public static int start_hour = 4;
    public static int start_minute = 0;
    //public static int repeat_interver = 1000 * 60 * 60 * 24; // 1 day
    public static int repeat_interver = 1000 * 60 * 30;

    public static Calendar getNextAlarm() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(LSPAlarmManager.nextAlarmTime);
        return cal;
    }


    private LSPAlarmManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    public static LSPAlarmManager getInstance(Context context) {
        if (alarmManager == null && context != null)
            instance = new LSPAlarmManager(context);
        return instance;
    }

    public void setTestTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + 1000 * 60 * 60 * 1);
        start_hour = calendar.get(Calendar.HOUR_OF_DAY);
        start_minute = calendar.get(Calendar.MINUTE);
    }

    public void setFirstAlarm() {
        setTestTime();   // for test...
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis()); //test code...
        Log.i(TAG, "start_hour "+start_hour + " minute "+start_minute);
        calendar.set(Calendar.HOUR_OF_DAY, start_hour);
        calendar.set(Calendar.MINUTE, start_minute);
        calendar.set(Calendar.SECOND, 0);
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        LSPAlarmManager.nextAlarmTime = calendar.getTimeInMillis();

        // LSPService intent
        //Intent intent = new Intent(context, LSPService.class);
        //intent.putExtra("requestCode", LSPService.ALARM_REQUEST);
        //alarmIntent = PendingIntent.getService(context, LSPService.ALARM_REQUEST, intent, 0);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, LSPAlarmManager.nextAlarmTime, alarmPendingIntent);
        Log.i(TAG, "set first alarm " + calendar.getTime());
    }

    public void clearAlarm() {
        if (alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            Log.i(TAG, "clearAlarm()");
        }
    }

    public void setNextAlarmAfter() {
        setNextAlarmAfter(LSPAlarmManager.repeat_interver);
    }

    public void setNextAlarmAfter(int millis) {
        // setTestTime();
        Calendar calendar = Calendar.getInstance();
        LSPAlarmManager.nextAlarmTime = calendar.getTimeInMillis() + millis;

        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, LSPAlarmManager.nextAlarmTime, alarmPendingIntent);
        Log.i(TAG, "setNextAlarmAfter alarm " + calendar.getTime());
    }

    /* deprecated... */
    public static void setNextAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        LSPAlarmManager.nextAlarmTime = LSPAlarmManager.nextAlarmTime + LSPAlarmManager.repeat_interver;
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime, alarmPendingIntent);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(LSPAlarmManager.nextAlarmTime);
        Log.i(TAG, "set next alarm " + calendar.getTime());
    }
}