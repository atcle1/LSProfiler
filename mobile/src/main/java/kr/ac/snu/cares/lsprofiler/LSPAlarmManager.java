package kr.ac.snu.cares.lsprofiler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import kr.ac.snu.cares.lsprofiler.receivers.AlarmReceiver;
import kr.ac.snu.cares.lsprofiler.util.AlarmTime;
import kr.ac.snu.cares.lsprofiler.util.FileLogWritter;

/**
 * Created by summer on 4/17/15.
 */
public class LSPAlarmManager {
    public static final String TAG = LSPAlarmManager.class.getSimpleName();
    private static AlarmManager alarmManager;
    private Context context;

    private static PendingIntent alarmPendingIntent;
    private BroadcastReceiver alarmReceiver = new AlarmReceiver();
    private static LSPAlarmManager instance;

    public static ArrayList<AlarmTime> alarmList = new ArrayList<>();
    public static AlarmTime nextAlarmTime;
    public static Calendar nextAlarmCal;
    public static long nextAlarmTimeMillis;

    //public static AlarmTime nextAlarmTimeMillis;
    //public static int start_hour = 4;
    //public static int start_minute = 0;
    //public static int repeat_interver = 1000 * 60 * 60 * 24; // 1 day

    public static Calendar getNextAlarm() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(LSPAlarmManager.nextAlarmTimeMillis);
        return cal;
    }


    private LSPAlarmManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < 24; ) {
            alarmList.add(new AlarmTime(i, 0));
            i+=6;
        }
    }

    private AlarmTime getNearestAlarm() {
        long current = System.currentTimeMillis();
        long minTerm = 24 * 60 * 60 * 1000;
        AlarmTime nextAlarmTime = null;
        if (alarmList == null || alarmList.size() == 0) {
            FileLogWritter.writeString("alarm list is null!!!");
            return new AlarmTime(0, 0);
        }
        for (int i = 0; i <  alarmList.size(); i++) {
            AlarmTime alarmTime = alarmList.get(i);
            long leftTime = alarmTime.getCallendar().getTimeInMillis() - current;
            if (leftTime < minTerm && leftTime > 60 * 1000) {
                minTerm = leftTime;
                nextAlarmTime = alarmTime;
            }
        }
        return nextAlarmTime;
    }

    public static LSPAlarmManager getInstance(Context context) {
        if (alarmManager == null && context != null)
            instance = new LSPAlarmManager(context);
        return instance;
    }

    public void setFirstAlarm() {
        nextAlarmTime = getNearestAlarm();
        nextAlarmCal = nextAlarmTime.getCallendar();
        nextAlarmTimeMillis = nextAlarmCal.getTimeInMillis();

        // LSPService intent
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        clearAlarm();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, LSPAlarmManager.nextAlarmTimeMillis, alarmPendingIntent);
        Log.i(TAG, "set first alarm " + nextAlarmCal.getTime());
    }

    public void clearAlarm() {
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            Log.i(TAG, "clearAlarm()");
        }
    }

    public void setNextAlarmAfter(int millis) {
        // setTestTime();
        Calendar calendar = Calendar.getInstance();
        LSPAlarmManager.nextAlarmTimeMillis = calendar.getTimeInMillis() + millis;

        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, LSPAlarmManager.nextAlarmTimeMillis, alarmPendingIntent);
        Log.i(TAG, "setNextAlarmAfter alarm " + calendar.getTime());
    }
}