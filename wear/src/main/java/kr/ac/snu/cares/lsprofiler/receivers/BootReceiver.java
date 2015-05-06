package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.service.LSPBootService;

/**
 * Created by summer on 4/26/15.
 */
public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = BroadcastReceiver.class.getSimpleName();
    public void onReceive(Context context, Intent intent) {
        //    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        Log.i(TAG, "BootReceiver - onRecive()");


        //}

        // power on
        LSPApplication app = LSPApplication.getInstance();
        if (app != null) {

            app.startProfilingIfStarted();
        } else {
            Log.i(TAG, "app is null");
        }
        Log.i(TAG, "BootReceiver - onRecive() before state change");
        LSPLog.onPowerStateChagned(1);
        //}
        Log.i(TAG, "BootReceiver - onRecive() before start service");
        Intent startServiceIntent = new Intent(context, LSPBootService.class);
        context.startService(startServiceIntent);
        Log.i(TAG, "BootReceiver - onRecive() end");
    }
}