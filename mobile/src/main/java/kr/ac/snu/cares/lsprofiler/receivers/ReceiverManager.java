package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.daemon.DaemonClientReader;

/**
 * Created by summer on 3/28/15.
 */
public class ReceiverManager extends BroadcastReceiver {
    public static final String TAG = DaemonClientReader.class.getSimpleName();
    private Context context;

    public ReceiverManager() {
        Log.e(TAG, "receiver manager ()");

    }

    public ReceiverManager(Context context) {
        this.context = context;
    }

    public void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        context.registerReceiver(this, filter);
        Log.i(TAG, "registerReceivers()");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_SCREEN_ON)){
            Log.i(TAG, "Screen ON");
        }
        else if(action.equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(TAG, "Screen OFF");
        } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            Log.i(TAG, action);
        } else {
            Log.i(TAG, action);
        }
    }
}