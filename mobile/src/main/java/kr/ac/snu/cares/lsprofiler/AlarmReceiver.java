package kr.ac.snu.cares.lsprofiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.service.LSPService;

/**
 * Created by summer on 4/18/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = BroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive()");

        // if service is not started...
        Intent startServiceIntent = new Intent(context, LSPService.class);
        startServiceIntent.putExtra("requestCode", LSPService.ALARM_REQUEST);
        context.startService(startServiceIntent);

        // set next alarm
        LSPAlarmManager.setNextAlarm(context);
    }
}