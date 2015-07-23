package kr.ac.snu.cares.lsprofiler;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.util.FileLogWritter;
import kr.ac.snu.cares.lsprofiler.util.Util;

/**
 * Created by summer on 3/28/15.
 */
public class LSPLog {
    public static final String TAG = LSPLog.class.getSimpleName();
    private static Context context;
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

    public static boolean checkDbHandler() {
        if (logDbHandler != null)
            return true;
        try {
            onException(new Exception("checkDbHandler() dbHandler is null"));
            if (logDbHandler == null)
                logDbHandler = ((LSPApplication) context.getApplicationContext()).getDbHandler();
        }catch (Exception ex) {
            onException(ex);
        }
        if (logDbHandler != null)
            return true;
        return false;
    }

    public static void onException(Exception ex) {
        /*
        String message = null;
        String logMsg = null;
        if (logDbHandler == null) {
            logDbHandler = ((LSPApplication) context.getApplicationContext()).getDbHandler();
        }
        if (logDbHandler != null) {
            try {
                message = Util.getStackTrace(ex);
                logMsg = "EXP " + Log.getStackTraceString(ex) + "\n" + message;

                logDbHandler.writeLog(logMsg);

            }catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        */
        try {
            FileLogWritter.writeException(ex);
        } catch (Exception ex3) {
            ex3.printStackTrace();
        }
    }

    public static void onWatchDog() {
        if(!bWriteLog) return;
        logDbHandler.writeLog("WATCHDOG ALIVE");
    }

    /* logging methods */
    public static void onTextMsg(String msg){
        if(!bWriteLog) return;
        logDbHandler.writeLog("TXT : " + msg);
    }

    public static void onTextMsgForce(String msg){
        if ( checkDbHandler()) {
            try {
                logDbHandler.writeLog("TFC : " + msg);
                FileLogWritter.writeString("TFC : " + msg);
                Log.e(TAG, "TFC " + msg);
            }catch (Exception ex) {
                onException(ex);
                ex.printStackTrace();
            }
        }
    }

    public static void onPowerStateChagned(int state) {
        logDbHandler.writeLog("PST : "+state);
    }

    public static void onScreenChagned(int onOff){
        if(!bWriteLog) return;
        logDbHandler.writeLog("SCR : "+onOff);
    }

    private static int prev_status = -1;
    private static int prev_chargePlug = -1;
    private static int prev_level = -1;
    public static void onBatteryStatusChagned(Intent intent) {
        if(!bWriteLog) return;
        LSPAlarmManager alarmManager = LSPAlarmManager.getInstance(context);
        if (alarmManager != null)
            alarmManager.setFirstAlarmIfNotSetted();
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
        int batteryPct = (int)(100 * level / (float)scale);

        if (prev_level != level || prev_status != status || prev_chargePlug != chargePlug) {
            prev_level = level;
            prev_status = status;
            prev_chargePlug = chargePlug;
            logDbHandler.writeLog("BAT : "+batteryPct+" "+statusStr+" "+plugStr + " " + temperature);
        }
    }

    public static void onLocationUpdate(Location loc) {
        if(!bWriteLog) return;
        Date fixed = new Date(loc.getTime());
        logDbHandler.writeLog("LOC_UPDATE : " + loc.getProvider() + " " + loc.getLatitude() + " " +  loc.getLongitude() + " " + loc.getAccuracy() + " " + fixed.toString());
    }

    public static void onKnownLocation(Location loc) {
        if(!bWriteLog) return;
        Date fixed = new Date(loc.getTime());
        logDbHandler.writeLog("LOC_KNOWN : " + loc.getProvider() + " " + loc.getLatitude() + " " + loc.getLongitude() + " " + loc.getAccuracy() + " " + fixed.toString());
    }


    public static void onNotificationPosted(StatusBarNotification sbn) {
        String title = "", text = "", bigtext="";
        String log = "";
        try {
            String packName = sbn.getPackageName();
            //if (sbn.getNotification().tickerText != null)
            //    ticker = sbn.getNotification().tickerText.toString();
            Bundle extras = sbn.getNotification().extras;
            if (extras != null) {
                /*
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    if (value != null)
                        Log.d(TAG, String.format("- %s %s (%s)", key, value.toString(), value.getClass().getName()));
                }
                */

                title = extras.getString("android.title");
                CharSequence textSequence = extras.getCharSequence("android.text");
                if (textSequence != null)
                    text = textSequence.toString();
                if (extras.containsKey("android.bigText"))
                    bigtext = extras.getCharSequence("android.bigText").toString();
            }
            Notification no = sbn.getNotification();
            Notification.Action actions[] = no.actions;

            if (title == null)
                title = "";
            if (text == null)
                text = "";
            if (bigtext == null)
                bigtext = "";

            if (!title.equals(""))
                title = title.length()+"|"+title.hashCode();
            if (!text.equals(""))
                text = text.length()+"|"+text.hashCode();
            if (!bigtext.equals(""))
                bigtext = bigtext.length()+"|"+bigtext.hashCode();

            log = "NOP : " + sbn.getKey() +  " onGoing=" + sbn.isOngoing()+" clearable="+sbn.isClearable()+" title="+ title + " text=" + text;
            if (bigtext.length() > 0)
                log  += " bigtext=" + bigtext;

            try {
                if (actions != null) {
                    log+=" |action=";
                    for (int i = 0; i < actions.length; i++)
                        log+="[" + actions[i].title + " " + actions[i].actionIntent.getCreatorPackage()+"]";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Log.i(TAG, log);
            if (!bWriteLog) return;
            logDbHandler.writeLog(log);
        } catch (Exception ex){
            ex.printStackTrace();
            onException(ex);
        }
    }

    // deprecated
    public static void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationRemoved " + sbn.toString());
        if(!bWriteLog) return;
        String packName = sbn.getPackageName();
        Log.i(TAG, "NOR : " + sbn.getPackageName() + "|" + sbn.getId());
        logDbHandler.writeLog("NOR : "+sbn.getKey());
    }

    public static void onNotificationPosted2(int status, StatusBarNotification sbn, StatusBarNotification oldsbn) {
        String title = "", text = "", bigtext="";
        String log = "";
        boolean bEnc = true;
        try {
            String packName = sbn.getPackageName();
            //if (sbn.getNotification().tickerText != null)
            //    ticker = sbn.getNotification().tickerText.toString();
            Bundle extras = sbn.getNotification().extras;
            if (extras != null) {
                title = extras.getString("android.title");
                CharSequence textSequence = extras.getCharSequence("android.text");
                if (textSequence != null)
                    text = textSequence.toString();
                if (extras.containsKey("android.bigText"))
                    bigtext = extras.getCharSequence("android.bigText").toString();
            }
            Notification no = sbn.getNotification();
            Notification.Action actions[] = no.actions;

            if (title == null)
                title = "";
            if (text == null)
                text = "";
            if (bigtext == null)
                bigtext = "";

            if (packName.equals("android") || packName.equals("com.android.vending") ||
                    packName.equals("com.android.systemui"))
                bEnc = false;

            if (!title.equals("") && bEnc)
                title = title.length()+"|"+title.hashCode();
            if (!text.equals("") && bEnc)
                text = text.length()+"|"+text.hashCode();
            if (!bigtext.equals("") && bEnc)
                bigtext = bigtext.length()+"|"+bigtext.hashCode();

            //system -1
            /** notification_enqueue status value for a newly enqueued notification. */
            //private static final int EVENTLOG_ENQUEUE_STATUS_NEW = 0;
            /** notification_enqueue status value for an existing notification. */
            //private static final int EVENTLOG_ENQUEUE_STATUS_UPDATE = 1;
            /** notification_enqueue status value for an ignored notification. */
            //private static final int EVENTLOG_ENQUEUE_STATUS_IGNORED = 2;

            switch (status) {
                case 0:
                    log += "NOP : ";
                    break;
                case 1:
                    log += "NOU : ";
                    break;
                case -1:
                    log += "NOS : ";
                    break;
                default:
                    log += "NOPP " + status + " : ";
                    break;

            }
            log += sbn.getKey() +  ";ing=" + sbn.isOngoing()+";cl="+sbn.isClearable()+";title="+ title + ";text=" + text;
            if (bigtext.length() > 0)
                log  += ";bigtext=" + bigtext;
            if (oldsbn != null) {
                log += ";oldtime="+Util.getTimeStringFromSystemMillis(oldsbn.getPostTime());
            }


            try {
                if (actions != null) {
                    log+=" |action=";
                    for (int i = 0; i < actions.length; i++)
                        log+=actions[i].title+";";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Log.i(TAG, log);
            if (!bWriteLog) return;
            logDbHandler.writeLog(Util.getTimeStringFromSystemMillis(sbn.getPostTime()), log);
        } catch (Exception ex){
            ex.printStackTrace();
            onException(ex);
            logDbHandler.writeLog(Util.getTimeStringFromSystemMillis(sbn.getPostTime()), log);
        }
    }


    public static void onNotificationCancel(String delTime, StatusBarNotification sbn, boolean sendDelete, int reason) {
        Log.i(TAG, "onNotificationCancel " + sbn.toString());
        if(!bWriteLog) return;
        String log = "NOC : ";
        log += sbn.getKey()+";" +
        Log.i(TAG, "NOC : " + sbn.getKey() + " id " + sbn.getId());
        logDbHandler.writeLog(delTime, "NOC : " + sbn.getKey() + ";sd=" + sendDelete + ";reason=" + reason + ";post="+Util.getTimeStringFromSystemMillis(sbn.getPostTime()));
    }

    public static void onCallStateChanged(int state, String incomingNumber) {
        if(!bWriteLog) return;
        String enc = Util.encryptData(incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                logDbHandler.writeLog("CST : RINGING " + enc);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                logDbHandler.writeLog("CST : OFFHOOK " + enc);
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                logDbHandler.writeLog("CST : IDLE " + enc);
                break;
        }
    }

    public static void onSmsReceived(Intent intent) {
        if(!bWriteLog) return;
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object messages[] = (Object[]) bundle.get("pdus");
        SmsMessage smsMessage[] = new SmsMessage[messages.length];

        for (int i = 0; i < messages.length; i++) {
            // PDU 포맷으로 되어 있는 메시지를 복원합니다.
            smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
        }
        // SMS 수신 시간 확인
        Date curDate = new Date(smsMessage[0].getTimestampMillis());
        //Log.d("문자 수신 시간", curDate.toString());
        // SMS 발신 번호 확인
        String origNumber = smsMessage[0].getOriginatingAddress();
        // SMS 메시지 확인
        String message = smsMessage[0].getMessageBody().toString();
        //Log.d("문자 내용", "발신자 : " + origNumber + ", 내용 : " + message);

        logDbHandler.writeLog("SMSR : " + Util.encryptData(origNumber) + " " + message.length()+"|"+message.hashCode());
    }

    /* package */
    public static void onPackageAdded(String packageName) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("PAD : " + packageName);
    }

    public static void onPackageRemoved(String packageName) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("PRM : " + packageName);
    }

    public static void onPackageReplaced(String packageName) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("PRP : " + packageName);
    }

    public static void onFitnessUniqueEvent(String msg){
        if(!bWriteLog) return;
        logDbHandler.writeLog2("FIT : " + msg);
    }

    /* framework logging */

    public static void onFTopActivityResuming(Intent intent) {
        if(!bWriteLog) return;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String strDT = Util.getTimeStringFromSystemMillis((bundle.getLong("time")));
            String message = (String) bundle.getString("message");
            logDbHandler.writeLog(strDT, "FFGA : " + message);

            Log.i(TAG, strDT + " FFGA " + message);
        }
    }

    public static void onNotification(Intent intent){
        if(!bWriteLog) return;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String strDT = Util.getTimeStringFromSystemMillis((bundle.getLong("time")));
            String message = (String) bundle.getString("message");
            String type = (String) bundle.getString("type");
            if (type != null) {
                if (type.equals("enq")) {
                    StatusBarNotification sbn = bundle.getParcelable("sbn");
                    StatusBarNotification oldsbn = bundle.getParcelable("oldsbn");
                    int enqueueStatus = bundle.getInt("status");
                    onNotificationPosted2(enqueueStatus, sbn, oldsbn);
                } else if (type.equals("cancel")) {
                    StatusBarNotification sbn = bundle.getParcelable("sbn");
                    Boolean sd = bundle.getBoolean("sd");
                    int reason = bundle.getInt("reason");
                    onNotificationCancel(strDT, sbn, sd, reason);
                } else {
                    Log.i(TAG, type + " : " + message);
                }
            } else {
                logDbHandler.writeLog(strDT, "NOT : " + type + " " + message);
                Log.i(TAG, strDT + " NOT : " + type + " " + message);
            }
        }
    }

    public static void onFPanel(Intent intent){
        if(!bWriteLog) return;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String strDT = Util.getTimeStringFromSystemMillis((bundle.getLong("time")));
            String message = (String) bundle.getString("message");
            logDbHandler.writeLog(strDT, "FPAN : " + message);

            Log.i(TAG, strDT + " FPAN " + message);
        }
    }

    public static void onFStatusbar(Intent intent){
        if(!bWriteLog) return;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String strDT = Util.getTimeStringFromSystemMillis((bundle.getLong("time")));
            String message = (String) bundle.getString("message");
            logDbHandler.writeLog(strDT, "FSTB : " + message);

            Log.i(TAG, strDT + " FSTB " + message);
        }
    }
}