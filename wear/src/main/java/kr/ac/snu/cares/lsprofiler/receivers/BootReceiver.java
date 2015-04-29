package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by summer on 4/26/15.
 */
public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = BroadcastReceiver.class.getSimpleName();
    public void onReceive(Context context, Intent intent) {
        //    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        Log.i("LP", "BootReceiver - onRecive()");


        //}
    }
}