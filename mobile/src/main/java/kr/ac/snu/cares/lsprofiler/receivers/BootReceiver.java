package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.service.LSPService;

/**
 * Created by summer on 3/24/15.
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("LP", "BootReceiver - onRecive()");
            Intent i = new Intent(context, LSPService.class);
            context.startService(i);
        }
    }
}