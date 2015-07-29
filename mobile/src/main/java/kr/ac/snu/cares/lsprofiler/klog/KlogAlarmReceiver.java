package kr.ac.snu.cares.lsprofiler.klog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPAlarmManager;
import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.LSPReporter;
import kr.ac.snu.cares.lsprofiler.service.LSPService;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;
import kr.ac.snu.cares.lsprofiler.util.Su;

/**
 * Created by summer on 4/18/15.
 */
public class KlogAlarmReceiver extends BroadcastReceiver {
    public static final String TAG = KlogAlarmReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        // Log.i(TAG, "onReceive() " + context.getPackageName());

        if (KlogAlarmManager.nextAlarmTimeMillis < System.currentTimeMillis() - 1000 * 60 * 60) {
            Log.i(TAG, "klog alarm expired more then 1 hour, ignored");
            KlogAlarmManager.getInstance(context).setFirstAlarm();
            return;
        }

        Log.i(TAG, "check kbuf");
        ReportItem item = new ReportItem();
        Su.executeSuOnce("/data/local/sprofiler 9 " + LSPReporter.COLLECT_MOBILE_PATH + " " + item.reportDateString + ".klog", 30000);


        // set next first alarm
        KlogAlarmManager.getInstance(context).setFirstAlarm();
        return;
    }
}