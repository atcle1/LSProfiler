package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.service.LSPBootService;
import kr.ac.snu.cares.lsprofiler.service.LSPService;
import kr.ac.snu.cares.lsprofiler.util.MyConsoleExe;

/**
 * Created by summer on 3/24/15.
 */
public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = BroadcastReceiver.class.getSimpleName();
    public void onReceive(Context context, Intent intent) {
    //    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        Log.i("LP", "BootReceiver - onRecive()");
        Intent i = new Intent(context, LSPService.class);
        context.startService(i);

        String stdout;
        MyConsoleExe exe = new MyConsoleExe();
        //stdout = exe.exec("su 0 setenforce 0", false);
        /*
        String arr[] = new String[1];
        arr[0] = "su 0 setenforce 0";
        exe.execSu(arr);
        */
        //Log.i(TAG, stdout);
        /*
        stdout = exe.exec("dumpsys batterystats --enable full-wake-history", true);
        Log.i(TAG, stdout);
        stdout = exe.exec("dumpsys batterystats --disable no-auto-reset", true);
        Log.i(TAG, stdout);
        */



        // power on
        LSPApplication app = LSPApplication.getInstance();
        if (app != null) {

            app.startProfilingIfStarted();
        } else {
            Log.i(TAG, "app is null");
        }
        LSPLog.onPowerStateChagned(1);
        //}

        Intent startServiceIntent = new Intent(context, LSPBootService.class);
        context.startService(startServiceIntent);
        Log.i("LP", "BootReceiver - onRecive() end");
    }
}