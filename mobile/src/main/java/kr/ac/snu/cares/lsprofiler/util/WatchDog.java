package kr.ac.snu.cares.lsprofiler.util;

import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 15. 5. 18.
 */
public class WatchDog extends Thread {
    public static final String TAG = WatchDog.class.getSimpleName();
    @Override
    public void run() {
        Log.i(TAG, "run()");
        while (true) {
            try {
                LSPLog.onWatchDog();
                Thread.sleep(60 * 1000);
            } catch (Exception ex) {
                FileLogWritter.writeString("watchdog exception");
                FileLogWritter.WriteException(ex);
            }
        }
    }
}
