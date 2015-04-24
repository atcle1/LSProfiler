package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.service.LSPService;
import kr.ac.snu.cares.lsprofiler.util.MyConsoleExe;
import kr.ac.snu.cares.lsprofiler.util.Su;

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
        LSPLog.onPowerStateChagned(1);
        Su su = new Su();
        su.prepare();
        su.execSu("su 0 setenforce 0");
        su.execSu("dumpsys batterystats --enable full-wake-history");
        su.execSu("dumpsys batterystats --disable no-auto-reset");
        su.execSu("/data/local/sprofiler 3 /sdcard/LSP/ test.klog");
        su.stopSu();
        //}
    }
}