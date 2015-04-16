package kr.ac.snu.cares.lsprofiler;

import android.content.Context;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;

/**
 * Created by summer on 3/28/15.
 */
public class LSPLog {
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
    public static void onBatteryStatusChagned(int a) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("BAT : "+a);
    }
    public static void onTeleponyStateChagned(double a){
        if(!bWriteLog) return;
        logDbHandler.writeLog("TEL : "+a);
    }
    public static void onScreenChagned(int on){
        if(!bWriteLog) return;
        logDbHandler.writeLog("SCR : "+on);
    }
    public static void onPowerStateChagned(int state) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("PST : "+state);
    }
    public static void onForegroundAppChagned(String packageName) {
        if(!bWriteLog) return;
        logDbHandler.writeLog("FAP : "+packageName);
    }
}