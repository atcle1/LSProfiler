package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by summer on 4/26/15.
 */
public class ShutdownReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {
        int i = 0;
        Log.v("ShutdownReceiver", "action - " + intent.getAction());




    }
}
