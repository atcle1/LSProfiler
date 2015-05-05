package kr.ac.snu.cares.lsprofiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.service.LSPService;

/**
 * Created by summer on 4/18/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = AlarmReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive() "+context.getPackageName());

        Boolean bRunning = LSPService.isServiceRunning(context);
        // if service is not started...
        if (bRunning == false) {
            Log.i(TAG, "onReceive() service isn't started, starting it...");
            Intent startServiceIntent = new Intent(context, LSPService.class);
            startServiceIntent.putExtra("requestCode", LSPService.ALARM_REQUEST);
            context.startService(startServiceIntent);
        } else {
            Log.i(TAG, "onReceive() service already started.");
            Handler handler = LSPService.getHandler();
            if (handler != null)
                handler.sendEmptyMessage(LSPService.ALARM_REQUEST);
        }

        // set next first alarm
        LSPAlarmManager.getInstance(context).setFirstAlarm();
    }
}