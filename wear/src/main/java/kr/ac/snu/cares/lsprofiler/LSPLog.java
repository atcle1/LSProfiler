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
    public static void onLocationUpdate(String provider, double lat, double lon) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("LOC : "+provider+" " +lat+" "+lon);
    }

    public static void onKnownLocation(String provider, double lat, double lon) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("ULC : " + provider + " "+lat+" "+lon);
    }

    private static int prev_status = -1;
    private static int prev_chargePlug = -1;
    private static int prev_batteryPct = -1;
    public static void onBatteryStatusChagned(Intent intent) {
        if(!bWriteLog) return;

        //int health= intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        //int icon_small= intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
        //boolean present= intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
        //String technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        int temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        //int voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String statusStr = "";
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            statusStr = "CHARGING";
        } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            statusStr = "DISCHARGING";
        } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
            statusStr = "FULL";
        } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            statusStr = "NOT_CHARGING";
        } else {
            statusStr = "UNKNOWN:"+status;
        }

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String plugStr = "";
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
            plugStr = "AC";
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
            plugStr = "USB";
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
            plugStr = "WIRELESS";
        } else {
            plugStr = "" + chargePlug;
        }

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (int)(level / (float)scale);

        if (prev_batteryPct != batteryPct || prev_status != status || prev_chargePlug != chargePlug) {
            prev_batteryPct = batteryPct;
            prev_status = status;
            prev_chargePlug = chargePlug;
            logDbHandler.writeLog("BAT : "+batteryPct+" "+statusStr+" "+plugStr + " " + temperature);
        }


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
        String title = "null", text = "null", bigtext="null";
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
            CharSequence textSequence = extras.getCharSequence("android.text");
            if (textSequence != null)
                text = textSequence.toString();
            if (extras.containsKey("android.bigText"))
                bigtext = extras.getCharSequence("android.bigText").toString();
        }

        //Log.i("id", "" + sbn.getId());
        //Log.i("Text", "tag "+sbn.getTag());
        Log.i("TAG", "NOP : "+sbn.getPackageName()+"|"+sbn.getId()+"|"+title.length()+"|"+text.length()+"|"+bigtext.length());
        if(!bWriteLog) return;
        logDbHandler.writeLog("NOP : "+sbn.getPackageName()+"|"+sbn.getId()+"|"+title.length()+"|"+text.length()+"|"+bigtext.length());

    }
    public static void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.i("id", "" + sbn.toString());
//        Log.i("id", "" + sbn.getId());
//        Log.i("Text", "tag "+sbn.getTag());
        if(!bWriteLog) return;
        String packName = sbn.getPackageName();
        Log.i(TAG, "NOR : "+sbn.getPackageName()+"|"+sbn.getId());
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

    public static void onTextMsg(String msg){
        if(!bWriteLog) return;
        logDbHandler.writeLog("TXT : " + msg);
    }

    public static void onTextMsgForce(String msg){
        if (logDbHandler != null) {
            try {
                logDbHandler.writeLog("TFC : " + msg);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}