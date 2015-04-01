package kr.ac.snu.cares.lsprofiler;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.pref.LSPPreferenceManager;
import kr.ac.snu.cares.lsprofiler.receivers.ReceiverManager;

/**
 * Created by summer on 3/28/15.
 */
public class LSPApplication extends Application {
    public static final String TAG = LSPApplication.class.getSimpleName();
    public static final String log_state[] = {"stop",
                                              "start",
                                              "pause",
                                              "stop-shutdown",
                                              "stop-send"};
    private LSPLoggerManager logMgr;
    private LogDbHandler dbHandler;
    private LSPPreferenceManager prefMgr;
    private ReceiverManager receiverManager;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHandler = new LogDbHandler(this);
        logMgr = new LSPLoggerManager(this);
        prefMgr = LSPPreferenceManager.getInstance(this);
        receiverManager = new ReceiverManager(this);
        Log.i(TAG, "onCreate()");

        receiverManager.registerReceivers();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "onTerminate()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "onLowMemory()");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigureationChanged()");
    }

    public LogDbHandler getDbHandler() {
        return dbHandler;
    }
}
