package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 4/26/15.
 */
public class ShutdownReceiver extends BroadcastReceiver{
    public static final String TAG = ShutdownReceiver.class.getSimpleName();
    public void onReceive(Context context, Intent intent) {
        int i = 0;
        Log.v(TAG, "action - " + intent.getAction());
        LSPLog.onPowerStateChagned(0);
        LSPApplication app = LSPApplication.getInstance();
        if (app != null)
            app.doKLogBackup();
    }
}
