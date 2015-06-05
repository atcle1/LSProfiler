package kr.ac.snu.cares.lsprofiler.receivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPAlarmManager;
import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.service.LSPService;

/**
 * Created by summer on 4/18/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = AlarmReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive() "+context.getPackageName());

        if (LSPAlarmManager.nextAlarmTimeMillis < System.currentTimeMillis() - 1000 * 60 * 60) {
            Log.i(TAG, "alarm expired more then 1 hour, ignored");
            LSPAlarmManager.getInstance(context).setFirstAlarm();
            return;
        }

        Boolean bRunning = LSPService.isServiceRunning(context);
        // if service is not started...
        if (bRunning == false) {
            Log.i(TAG, "onReceive() service isn't started, starting it...");
            Intent startServiceIntent = new Intent(context, LSPService.class);
            startServiceIntent.putExtra("requestCode", LSPService.ALARM_REQUEST);
            context.startService(startServiceIntent);
            LSPLog.onTextMsgForce("AlarmReceiver() bRunning false start service");
        } else {
            Log.i(TAG, "onReceive() service already started.");
            Handler handler = LSPService.getHandler();
            if (handler != null) {
                handler.sendEmptyMessage(LSPService.ALARM_REQUEST);
                LSPLog.onTextMsgForce("AlarmReceiver() bRunning sendMessage");
            } else {
                LSPLog.onTextMsgForce("AlarmReceiver() bRunning bug handler is null");
            }
        }

        // set next first alarm
        LSPAlarmManager.getInstance(context).setFirstAlarm();
    }
}