package kr.ac.snu.cares.lsprofiler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;


import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.pref.LSPPreferenceManager;
import kr.ac.snu.cares.lsprofiler.receivers.ReceiverManager;
import kr.ac.snu.cares.lsprofiler.service.WSLPReportService;
import kr.ac.snu.cares.lsprofiler.util.DeviceID;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;
import kr.ac.snu.cares.lsprofiler.util.Su;

/**
 * Created by summer on 3/28/15.
 */
public class LSPApplication extends Application {
    public static final String TAG = LSPApplication.class.getSimpleName();
    private static LSPApplication app;
    public enum State {stopped, started, resumed, paused, error};
    public State state = State.stopped;
    private Su su;
    PowerManager pm;

    private LSPReporter reporter;

    //private DaemonClient clientHandler;
    //HandlerThread daemonClientThread;

    private LogDbHandler dbHandler;
    private LSPPreferenceManager prefMgr;

    private LSPLog lspLog;

    private ReceiverManager receiverManager;

    public String deviceID;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        Context context = getApplicationContext();
        dbHandler = new LogDbHandler(context);
        lspLog = new LSPLog(context);
        prefMgr = LSPPreferenceManager.getInstance(context);
        receiverManager = new ReceiverManager(context);

        deviceID = prefMgr.getDeviceID();
        if (deviceID.equals("")) {
            deviceID = DeviceID.getDeviceID(this);
            prefMgr.setDeviceID(deviceID);
        }

        reporter = new LSPReporter(this);
        pm =  (PowerManager) getSystemService( Context.POWER_SERVICE );

        /*
        daemonClientThread = new HandlerThread("daemon client thread");
        daemonClientThread.start();
        clientHandler = new DaemonClient(daemonClientThread.getLooper());
        clientHandler.init(this);
        */
        app = this;
    }

    public LogDbHandler getDbHandler() {
        return dbHandler;
    }
    public static LSPApplication getInstance() { return LSPApplication.app; }

    public void startProfiling() {
        Log.i(TAG, "startProfiling()");
        prefMgr.setLoggingState("start");
        startLogging();
    }
    public void stopProfiling() {
        Log.i(TAG, "stopProfiling()");
        prefMgr.setLoggingState("stop");
        stopLogging();
    }
    public void startProfilingIfStarted() {
        String savedState = prefMgr.getLoggingState();
        if (savedState.equals("start")) {
            Log.i(TAG, "startProfilingIfStarted() start profiling...");
            startProfiling();
        } else {
            Log.i(TAG, "startProfilingIfStarted() not start");
        }
    }

    public void startLogging() {
        Log.i(TAG, "startLogging()");
        if (state != State.stopped) {
            Log.i(TAG, "startLogging() : not stopped");
            return;
        }
        state = State.started;
        prefMgr.setAppState(State.started.name());
        // start service
        //Intent startServiceIntent = new Intent(this, LSPService.class);

        //startServiceIntent.putExtra("setting", setting);
        //startServiceIntent.putExtra("first_start", true);
        //startService(startServiceIntent);

        //alarmManager.setFirstAlarm();

        resumeLogging();
        LSPLog.onTextMsg("startLogging()");

    }
    public void resumeLogging() {
        showToast("resumeLogging()");
        Log.i(TAG, "resumeLogging()");
        LSPLog.onTextMsg("resumeLogging() "+ Calendar.getInstance().getTime().toString());
        state = State.resumed;
        try {
            lspLog.resumeLogging();
            receiverManager.registerReceivers();
            //locationTracker.startTrace();
            //LSPNotificationService.startSelf(this);
        }catch (Exception ex) {
            ex.printStackTrace();;
            LSPLog.onTextMsg(ex.getLocalizedMessage());
        }
    }

    public void pauseLogging() {
        showToast("pauseLogging()");
        Log.i(TAG, "pauseLogging()");
        LSPLog.onTextMsg("pauseLogging() "+ Calendar.getInstance().getTime().toString());
        if (state != State.resumed) {
            Log.i(TAG, "pauseLogging() : not resumed");
            return;
        }
        state = State.paused;
        receiverManager.unregisterReceivers();
        //locationTracker.stopTrace();
        //LSPNotificationService.stopSelf(this);
        lspLog.pauseLogging();
    }

    public void stopLogging() {
        LSPLog.onTextMsg("stopLogging()");
        if (state == State.resumed)
            pauseLogging();

        if (state != State.paused) {
            Log.i(TAG, "stopLogging() : not paused");
            return;
        }

        state = State.stopped;
        prefMgr.setAppState(State.stopped.name());

        //alarmManager.clearAlarm();
        //stopService(new Intent(this, LSPService.class));
    }

    public void doWearReport(String mac) {
        Intent startServiceIntent = new Intent(this, WSLPReportService.class);
        startServiceIntent.putExtra("serverMac", mac);
        startServiceIntent.putExtra("resumeWhenFinished", true);
        pauseLogging();
        startService(startServiceIntent);
    }

    public void doKLogBackup() {
        ReportItem item = new ReportItem();
        reporter.requestReportToDaemon(item);
    }

    @Override
    public void onTerminate() {
        LSPLog.onTextMsgForce("APP onTerminate()");
        showToast("onTerminate()");
        super.onTerminate();
        Log.i(TAG, "onTerminate()");
        receiverManager.unregisterReceivers();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "onLowMemory()");
        LSPLog.onTextMsgForce("APP onLowMemory()");
        showToast("onLowMemory()");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigureationChanged()");
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}