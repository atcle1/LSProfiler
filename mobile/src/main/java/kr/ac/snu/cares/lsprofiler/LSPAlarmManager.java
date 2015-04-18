package kr.ac.snu.cares.lsprofiler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by summer on 4/17/15.
 */
public class LSPAlarmManager {
    public static final String TAG = LSPAlarmManager.class.getSimpleName();
    private AlarmManager alarmManager;
    private Context context;
    private static long nextAlarmTime;
    private PendingIntent alarmIntent;
    private BroadcastReceiver alarmReceiver = new AlarmReceiver();

    public static int start_hour = 14;
    public static int start_minute = 0;
    public static int repeat_interver = 1000 * 60 * 60 * 24;


    public LSPAlarmManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setFirstAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, start_hour);
        calendar.set(Calendar.MINUTE, start_minute);
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // LSPService intent
        //Intent intent = new Intent(context, LSPService.class);
        //intent.putExtra("requestCode", LSPService.ALARM_REQUEST);
        //alarmIntent = PendingIntent.getService(context, LSPService.ALARM_REQUEST, intent, 0);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    public void setNextAlarm() {
        // set next alarm
    }

    public void clearAlarm() {
        if (alarmIntent != null) {
            alarmManager.cancel(alarmIntent);
            alarmManager = null;
        }
    }

    public static void setNextAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        LSPAlarmManager.nextAlarmTime = LSPAlarmManager.nextAlarmTime + LSPAlarmManager.repeat_interver;
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextAlarmTime, alarmPendingIntent);
    }
}
