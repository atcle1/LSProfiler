package kr.ac.snu.cares.lsprofiler;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.telephony.TelephonyManager;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.util.Util;

/**
 * Created by summer on 3/28/15.
 */
public class LSPLog {
    public static final String TAG = LSPLog.class.getSimpleName();
    private Context context;
    private static boolean bWriteLog = false;
    private static LogDbHandler logDbHandler;

    public LSPLog(Context context) {
        this.context = context;
        logDbHandler = ((LSPApplication) context.getApplicationContext()).getDbHandler();
    }

    public void pauseLogging() {
        bWriteLog = false;
    }

    public void resumeLogging() {
        bWriteLog = true;
    }

    private void writeAllLogOnFile() {

    }

    /* logging methods */
    public static void onLocationChanged(double lat, double lon) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("LOC : "+lat+" "+lon);

    }
    public static void onBatteryStatusChagned(Intent intent) {
        if(!bWriteLog) return;

        int health= intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
        int icon_small= intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL,0);
        int level= intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
        int plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
        boolean present= intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
        int scale= intent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
        int status= intent.getIntExtra(BatteryManager.EXTRA_STATUS,0);
        String technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        int temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
        int voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
        logDbHandler.writeLog("BAT : "+plugged +" "+status+" "+level+" "+temperature);
    }
    public static void onTeleponyStateChagned(double a){
        if(!bWriteLog) return;
        logDbHandler.writeLog("TEL : "+a);
    }
    public static void onScreenChagned(int onOff){
        if(!bWriteLog) return;
        logDbHandler.writeLog("SCR : "+onOff);
    }
    public static void onPowerStateChagned(int state) {
        //if(!bWriteLog) return;
        logDbHandler.writeLog("PST : "+state);
    }
    public static void onForegroundAppChagned(String packageName) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("FAP : "+packageName);
    }
    public static void onNotificationPosted(StatusBarNotification sbn) {
        String title = "", text = "", bigtext="";
        String packName = sbn.getPackageName();
        //if (sbn.getNotification().tickerText != null)
        //    ticker = sbn.getNotification().tickerText.toString();
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            /*
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value != null)
                    Log.d(TAG, String.format("- %s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
            */
            title = extras.getString("android.title");
            text = extras.getCharSequence("android.text").toString();
            if (extras.containsKey("android.bigText"))
                bigtext = extras.getCharSequence("android.bigText").toString();
        }

        Log.i("id", "" + sbn.getId());
        Log.i("Text", "tag "+sbn.getTag());
        if(!bWriteLog) return;
        logDbHandler.writeLog("NOP : "+sbn.getPackageName()+"|"+sbn.getId()+"|"+title+"|"+text+"|"+bigtext);

    }
    public static void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.i("id", "" + sbn.toString());
//        Log.i("id", "" + sbn.getId());
//        Log.i("Text", "tag "+sbn.getTag());
        if(!bWriteLog) return;
        String packName = sbn.getPackageName();
        logDbHandler.writeLog("NOR : "+sbn.getPackageName()+"|"+sbn.getId());
    }
    public static void onCallStateChanged(int state, String incomingNumber) {
        if(!bWriteLog) return;
        String enc = Util.encryptData(incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                logDbHandler.writeLog("CST : RINGING " + enc);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                logDbHandler.writeLog("CST : OFFHOOK" + enc);
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                logDbHandler.writeLog("CST : IDLE" + enc);
                break;
        }
    }
    public static void onTopActivityResuming(String packageName) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("FGA : " + packageName);
    }
}