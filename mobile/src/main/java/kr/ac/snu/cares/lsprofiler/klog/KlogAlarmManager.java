package kr.ac.snu.cares.lsprofiler.klog;

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
public class KlogAlarmManager {
    public static final String TAG = KlogAlarmManager.class.getSimpleName();
    private static AlarmManager alarmManager;
    private Context context;

    private static PendingIntent alarmPendingIntent;
    private static KlogAlarmManager instance;

    public static ArrayList<AlarmTime> alarmList = new ArrayList<>();
    public static AlarmTime nextAlarmTime;
    public static Calendar nextAlarmCal;
    public static long nextAlarmTimeMillis;

    public static boolean bAlarmEnabled = true;

    public static Calendar getNextAlarm() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(KlogAlarmManager.nextAlarmTimeMillis);
        return cal;
    }

    private KlogAlarmManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        // 4hour term
        for (int i = 1; i < 24; i+=3) {
            alarmList.add(new AlarmTime(i, 0));
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

    public static KlogAlarmManager getInstance(Context context) {
        if (alarmManager == null && context != null)
            instance = new KlogAlarmManager(context);
        return instance;
    }

    public void setFirstAlarm() {
        if (bAlarmEnabled == false) return;
        //setNextAlarmAfter(10 * 1000);
        //if (true) return;
        nextAlarmTime = getNearestAlarm();
        nextAlarmCal = nextAlarmTime.getCallendar();
        nextAlarmTimeMillis = nextAlarmCal.getTimeInMillis();

        // LSPService intent
        Intent intent = new Intent(context, KlogAlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        clearAlarm();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, KlogAlarmManager.nextAlarmTimeMillis, alarmPendingIntent);
        Log.i(TAG, "Klog set first alarm " + nextAlarmCal.getTime());
        FileLogWritter.writeString("Klog set first alarm " + nextAlarmCal.getTime());
    }

    public void setFirstAlarmIfNotSetted() {
        if (bAlarmEnabled == false) return;
        if (nextAlarmTimeMillis < System.currentTimeMillis()) {
            setFirstAlarm();
        }
    }

    public void clearAlarm() {
        if (bAlarmEnabled == false) return;
        Intent intent = new Intent(context, KlogAlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            Log.i(TAG, "Klog clearAlarm()");
        }
    }

    // for debugging
    public void setNextAlarmAfter(int millis) {
        if (bAlarmEnabled == false) return;
        // setTestTime();
        Calendar calendar = Calendar.getInstance();
        KlogAlarmManager.nextAlarmTimeMillis = calendar.getTimeInMillis() + millis;

        Intent intent = new Intent(context, KlogAlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, KlogAlarmManager.nextAlarmTimeMillis, alarmPendingIntent);
        Log.i(TAG, "setNextAlarmAfter alarm " + calendar.getTime());
    }
}