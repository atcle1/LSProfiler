package kr.ac.snu.cares.lsprofiler;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.pref.LSPPreferenceManager;
import kr.ac.snu.cares.lsprofiler.receivers.LocationTracer;
import kr.ac.snu.cares.lsprofiler.receivers.ReceiverManager;
import kr.ac.snu.cares.lsprofiler.service.LSPNotificationService;

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
    private LogDbHandler dbHandler;
    private LSPPreferenceManager prefMgr;

    private LSPLog lspLog;

    private ReceiverManager receiverManager;
    private LocationTracer locationTracker;
    private LSPNotificationService notificationService;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHandler = new LogDbHandler(this);
        lspLog = new LSPLog(this);
        prefMgr = LSPPreferenceManager.getInstance(this);
        receiverManager = new ReceiverManager(this);
        locationTracker = new LocationTracer(this);
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

    public void startLogging() {
        lspLog.resumeLogging();
        receiverManager.registerReceivers();
        locationTracker.startTrace();
        notificationService.startSelf(this);
    }

    public void stopLogging() {
        lspLog.pauseLogging();
        receiverManager.unregisterReceivers();
        locationTracker.stopTrace();
        notificationService.stopSelf(this);
    }
}
