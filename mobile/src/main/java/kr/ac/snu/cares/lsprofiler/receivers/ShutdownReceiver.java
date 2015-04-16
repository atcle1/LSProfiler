package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 3/26/15.
 */
public class ShutdownReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {
        int i = 0;
        Log.v("ShutdownReceiver", "ver - " + intent.getAction());

        while (i++ < 1000) {
            // test :)
            try {
                Log.v("ShutdownReceiver", "ver - " + intent.getAction() + " " + i);
                Thread.sleep(100);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // power off
        LSPLog.onPowerStateChagned(0);
    }
}
