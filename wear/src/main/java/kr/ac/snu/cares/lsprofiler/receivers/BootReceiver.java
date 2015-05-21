package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.service.LSPBootService;
import kr.ac.snu.cares.lsprofiler.service.WLSPService;

/**
 * Created by summer on 4/26/15.
 */
public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = BroadcastReceiver.class.getSimpleName();
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BootReceiver - onRecive()");

        Intent startServiceIntent = new Intent(context, LSPBootService.class);
        context.startService(startServiceIntent);

        // WLSP service
        Intent i = new Intent(context, WLSPService.class);
        context.startService(i);

        // power on
        LSPApplication app = LSPApplication.getInstance();
        if (app != null) {
            app.startProfilingIfStarted();
        } else {
            Log.e(TAG, "app is null");
        }

        Log.i(TAG, "BootReceiver - onRecive() before state change");
        LSPLog.onPowerStateChagned(1);
    }
}