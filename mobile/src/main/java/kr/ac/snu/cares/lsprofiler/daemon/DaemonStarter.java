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
    public static void startKernelProfile() {
        startDaemon("1");
    }
    public static void stopKernelProfile() {
        startDaemon("2");
    }
    public static void startForReport(String dirPath, String fileName) {
        startDaemon("3 " + dirPath + " " + fileName);
    }
    public static void startDaemon(String args) {
        MyConsoleExe exe = new MyConsoleExe();
        String stdout;

        stdout = exe.exec("/data/local/sprofiler " + args, true);

        Log.i(TAG, "stdout : "+stdout);
    }
}
