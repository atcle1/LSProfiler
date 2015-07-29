package kr.ac.snu.cares.lsprofiler;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        StringBuilder logBuilder = new StringBuilder();
        boolean bEnc = true;
        try {
            Notification notification = sbn.getNotification();
            String packName = sbn.getPackageName();

            switch (status) {
                case 0:
                    logBuilder.append("NOP : ");
                    break;
                case 1:
                    logBuilder.append("NOU : ");
                    break;
                case -1:
                    logBuilder.append("NOS : ");
                    break;
                default:
                    logBuilder.append("NOPP " + status + " : ");
                    break;
            }


            if (packName.equals("android") || packName.equals("com.android.vending") ||
                    packName.equals("com.android.systemui"))
                bEnc = false;
            bEnc = false;

/*
            if (!title.equals("") && bEnc)
                title = title.length()+"|"+title.hashCode();
            if (!text.equals("") && bEnc)
                text = text.length()+"|"+text.hashCode();
            if (!bigtext.equals("") && bEnc)
                bigtext = bigtext.length()+"|"+bigtext.hashCode();
            */

            //system -1
            /** notification_enqueue status value for a newly enqueued notification. */
            //private static final int EVENTLOG_ENQUEUE_STATUS_NEW = 0;
            /** notification_enqueue status value for an existing notification. */
            //private static final int EVENTLOG_ENQUEUE_STATUS_UPDATE = 1;
            /** notification_enqueue status value for an ignored notification. */
            //private static final int EVENTLOG_ENQUEUE_STATUS_IGNORED = 2;


            logBuilder.append(sbn.getKey() +  ";ing=" + sbn.isOngoing()+";cl="+sbn.isClearable());

            logBuilder.append(";bEnc="+bEnc);
            try {
                extrasAnalysis(logBuilder, notification, bEnc);
            } catch (Exception ex) {
                LSPLog.onException(ex);
            }

            if (oldsbn != null) {
                logBuilder.append(";oldtime="+Util.getTimeStringFromSystemMillis(oldsbn.getPostTime()));
            }

            long[] vibration = sbn.getNotification().vibrate;
            if (vibration != null) {
                logBuilder.append(";vib=");
                for (int i = 0; i < vibration.length; i++) {
                    logBuilder.append(vibration[i]);
                    if (i != vibration.length - 1) {
                        logBuilder.append(",");
                    }
                }
            }

            int ledOff = notification.ledOffMS;
            int ledOn = notification.ledOnMS;
            int rgb = notification.ledARGB;
            if (ledOff !=0 || ledOn != 0) {
                logBuilder.append(";led=" + rgb + ";ledOn=" + ledOn +";ledOff=" + ledOff);
            }

            if (notification.largeIcon != null)
                logBuilder.append(";lis=" + notification.largeIcon.getAllocationByteCount());

            Notification.Action actions[] = notification.actions;
            if (actions != null) {
                logBuilder.append(";action=");
                for (int i = 0; i < actions.length; i++) {
                    logBuilder.append(actions[i].title);
                    if (i != actions.length -1) {
                        logBuilder.append(",");
                    }
                }
            }

            Log.i(TAG, logBuilder.toString());
            if (!bWriteLog) return;
            logDbHandler.writeLog(Util.getTimeStringFromSystemMillis(sbn.getPostTime()), logBuilder.toString());
        } catch (Exception ex){
            ex.printStackTrace();
            onException(ex);
            logDbHandler.writeLog(Util.getTimeStringFromSystemMillis(sbn.getPostTime()), logBuilder.toString());
        }
    }

    public static void extrasAnalysis(StringBuilder logBuilder, Notification notification, boolean bEnc) {
        Bundle extras = notification.extras;
        // Log.i(TAG, "extras size : " + extras.size());

        if (extras.size() == 0) {
            logBuilder.append(";extras=" + 0);
            return;
        }

        // template style
        String template = extras.getString("android.template");
        //Log.i(TAG, "template " + template);
        if (template != null) {
            if (template.contains("InboxStyle")) {
                logBuilder.append(";style="+"Inbox");
            } else if (template.contains("BigTextStyle")) {
                logBuilder.append(";style="+"BigText");
            } else if (template.contains("BigPictureStyle")) {
                logBuilder.append(";style="+"BigPicture");
            } else if (template.contains("MediaStyle")) {
                logBuilder.append(";style="+"MediaStyle");
            } else {
                logBuilder.append(";style="+template);
            }
        } else {
            logBuilder.append(";style=null");
        }

        String temp;
        if (extras.containsKey("android.title")) {
            try {
                temp = ObjToString(extras.get("android.title"));
                if (bEnc) temp = temp.length() + "|" + temp.hashCode();
                logBuilder.append(";title=" + temp);
            }catch (Exception ex) {}
        }

        if (extras.containsKey("android.text")) {
            try {
                if (extras.get("android.text") != null) {
                    temp = ObjToString(extras.get("android.text"));
                    if (bEnc) temp = temp.length() + "|" + temp.hashCode();
                    logBuilder.append(";text=" + temp);
                }
            }catch (Exception ex) {}
        }

        if (extras.containsKey("android.subText")) {
            try {
                if (extras.get("android.subText") != null) {
                    temp = ObjToString(extras.get("android.subText"));
                    if (bEnc) temp = temp.length() + "|" + temp.hashCode();
                    logBuilder.append(";subText=" + temp);
                }
            }catch (Exception ex) {}
        }

        if (extras.containsKey("android.bigText")) {
            try {
                temp = ObjToString(extras.get("android.bigText"));
                if (bEnc) temp = temp.length() + "|" + temp.hashCode();
                logBuilder.append(";bigText=" + temp);
            }catch (Exception ex) {}
        }

        if (extras.containsKey("android.title.big")) {
            try {
                temp = ObjToString(extras.get("android.title.big"));
                if (bEnc) temp = temp.length() + "|" + temp.hashCode();
                logBuilder.append(";titleBig=" + temp);
            }catch (Exception ex) {}
        }

        if (extras.containsKey("android.textLines")) {
            try {
                java.lang.CharSequence[] seqs= (java.lang.CharSequence[]) extras.get("android.textLines");
                if (seqs != null) {
                    int totalLen = 0;
                    for (int i = 0; i < seqs.length; i++) {
                        totalLen += seqs[i].length();
                        //Log.i(TAG, i + " : "+seqs[i]);
                    }
                    logBuilder.append(";tLines=" + totalLen);
                }
            }catch (Exception ex) {}
        }

        if (extras.containsKey("android.summaryText")) {
            try {
                if (extras.get("android.summaryText") != null) {
                    temp = ObjToString(extras.get("android.summaryText"));
                    if (bEnc) temp = temp.length() + "|" + temp.hashCode();
                    logBuilder.append(";smtext=" + temp);
                }
            }catch (Exception ex) {}
        }

        if (extras.containsKey("android.picture")) {
            try {
                Bitmap picture = (Bitmap) extras.get("android.picture");
                if (picture != null) {
                    int size = picture.getAllocationByteCount();
                    logBuilder.append(";psize=" + size);
                }
            }catch (Exception ex) {}
        }

        try {
            int progressMax = extras.getInt("android.progressMax", 0);
            int progress;
            if (progressMax != 0) {
                progress = extras.getInt("android.progress", 0);
                logBuilder.append(";progress=" + progress + "/" + progressMax);
            }
        } catch (Exception ex) {

        }

        if (extras.containsKey("android.wearable.EXTENSIONS")) {
            Bundle Eextras = (Bundle)extras.get("android.wearable.EXTENSIONS");
            if (Eextras != null) {
                logBuilder.append(";wExt=[");
                try {
                    wearExtentionAnalysis(logBuilder, Eextras, bEnc);
                }catch (Exception ex) {
                    LSPLog.onException(ex);
                }
                logBuilder.append("]");
            }
        }


        /*
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value != null)
                    Log.d(TAG, String.format("Extras - %s : %s (%s)\n", key, value.toString(), value.getClass().getName()));
            }
        }
        */

    }

    public static void wearExtentionAnalysis(StringBuilder logBuilder, Bundle extras, boolean bEnc) {
        /*
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            if (value != null)
                Log.d(TAG, String.format("WEAR Extention - %s : %s (%s)\n", key, value.toString(), value.getClass().getName()));
        }
        */
        boolean bFirst = true;
        String temp;

        // wear background
        if (extras.containsKey("background")) {
            try {
                Bitmap picture = (Bitmap) extras.get("background");
                if (picture != null) {
                    int size = picture.getAllocationByteCount();
                    logBuilder.append("wbgsize=" + size);
                    bFirst = false;
                }
            }catch (Exception ex) {}
        }

        // wear page
        if (extras.containsKey("pages")) {
            try {
                Parcelable[] pas = extras.getParcelableArray("pages");
                if (pas != null) {
                    //Log.i(TAG, "pas len = " + pas.length);
                    if (!bFirst) logBuilder.append(";");
                    logBuilder.append("pages=[");

                    for (int i = 0; i < pas.length; i++) {
                        logBuilder.append(i + ":");
                        Notification notiPage = (Notification) pas[i];
                        Bundle subNotiExtras = notiPage.extras;
                        boolean bwFirst = true;

                        /*
                        for (String key : subNotiExtras.keySet()) {
                            Object value = subNotiExtras.get(key);
                            if (value != null)
                                Log.d(TAG, String.format("page - extras - %s : %s (%s)\n", key, value.toString(), value.getClass().getName()));
                        }
                        */

                        if (subNotiExtras.containsKey("android.title")) {
                            temp = ObjToString(subNotiExtras.get("android.title"));
                            if (!temp.equals("null")) {
                                if (bEnc) temp = temp.length() + "";
                                logBuilder.append("title="+temp);
                                bwFirst = false;
                            }
                        }

                        if (subNotiExtras.containsKey("android.text")) {
                            temp = ObjToString(subNotiExtras.get("android.text"));
                            if (!temp.equals("null")) {
                                if (bEnc) temp = temp.length() + "";
                                if (!bFirst) logBuilder.append(";");
                                logBuilder.append(";text=" + temp);
                                bwFirst = false;
                            }
                        }

                        if (subNotiExtras.containsKey("android.bigText")) {
                            temp = ObjToString(subNotiExtras.get("android.bigText"));
                            if (!temp.equals("null")) {
                                if (bEnc) temp = temp.length() + "";
                                if (!bFirst) logBuilder.append(";");
                                logBuilder.append("bigText=" + temp);
                                bwFirst = false;
                            }
                        }

                        // page wear extention
                        if (subNotiExtras.containsKey("android.wearable.EXTENSIONS")) {
                            Bundle subNotiExtrasEx = subNotiExtras.getBundle("android.wearable.EXTENSIONS");


                            /*
                            for (String key : subNotiExtrasEx.keySet()) {
                                Object value = subNotiExtrasEx.get(key);
                                if (value != null)
                                    Log.d(TAG, String.format("page - extras - wearExt - %s : %s (%s)\n", key, value.toString(), value.getClass().getName()));
                            }
                            */

                            if (subNotiExtras != null && subNotiExtrasEx.containsKey("background")) {
                                try {
                                    Bitmap picture = (Bitmap) extras.get("background");
                                    int size = picture.getAllocationByteCount();
                                    logBuilder.append("(pbgsize:" + size + ")");
                                } catch (Exception ex) {
                                }
                            }
                        }

                        if (i < pas.length - 1) {
                            logBuilder.append(",");
                        }
                        // page end
                    }
                }
            }catch (Exception ex) {
                ex.printStackTrace();
            }
            logBuilder.append("]");
        }
    }

    public static String ObjToString(Object obj) {
        if (obj != null) {
            return obj.toString().replaceAll("\\[","(").replaceAll("\\]", ")").replaceAll(";", ":");
        } else
            return "null";
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

            // Log.i(TAG, strDT + " FPAN " + message);
        }
    }

    public static void onFStatusbar(Intent intent){
        if(!bWriteLog) return;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String strDT = Util.getTimeStringFromSystemMillis((bundle.getLong("time")));
            String message = (String) bundle.getString("message");
            logDbHandler.writeLog(strDT, "FSTB : " + message);

            //Log.i(TAG, strDT + " FSTB " + message);
        }
    }
}