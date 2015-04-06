package kr.ac.snu.cares.lsprofiler.daemon;

import android.util.Log;

import kr.ac.snu.cares.lsprofiler.util.MyConsoleExe;

/**
 * Created by summer on 4/3/15.
 */
public class DaemonStarter {
    public static final String TAG = DaemonStarter.class.getSimpleName();
    public static void startDaemon() {
        MyConsoleExe exe = new MyConsoleExe();
        String stdout;
        //String stdout = exe.exec("dumpsys batterystats --enable full-wake-history", false);
        //Log.i(TAG, stdout);
        //stdout = exe.exec("dumpsys batterystats --disable no-auto-reset", false);
        //Log.i(TAG, stdout);
        stdout = exe.exec("/data/local/sprofiler 6", false);

        //stdout = exe.exec("/data/local/sprofiler 6", false);
        // [2] 5965
        Log.i(TAG, stdout);
    }
}
