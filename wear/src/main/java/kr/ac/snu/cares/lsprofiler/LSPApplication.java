package kr.ac.snu.cares.lsprofiler;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;


import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.klog.KlogAlarmManager;
import kr.ac.snu.cares.lsprofiler.pref.LSPPreferenceManager;
import kr.ac.snu.cares.lsprofiler.receivers.ReceiverManager;
import kr.ac.snu.cares.lsprofiler.service.WSLPReportService;
import kr.ac.snu.cares.lsprofiler.util.DeviceID;
import kr.ac.snu.cares.lsprofiler.util.FileLogWritter;
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
    private PowerManager.WakeLock toastWl;

    private LSPReporter reporter;

    //private DaemonClient clientHandler;
    //HandlerThread daemonClientThread;

    private LogDbHandler dbHandler;
    private LSPPreferenceManager prefMgr;

    private LSPLog lspLog;

    private ReceiverManager receiverManager;
    private KlogAlarmManager klogAlarmManager;

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
        klogAlarmManager = KlogAlarmManager.getInstance(context);

        deviceID = prefMgr.getDeviceID();
        if (deviceID.equals("")) {
            deviceID = DeviceID.getDeviceID(this);
            prefMgr.setDeviceID(deviceID);
        }

        reporter = new LSPReporter(this);
        pm =  (PowerManager) getSystemService( Context.POWER_SERVICE );
        toastWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "toast wl");
        /*
        daemonClientThread = new HandlerThread("daemon client thread");
        daemonClientThread.start();
        clientHandler = new DaemonClient(daemonClientThread.getLooper());
        clientHandler.init(this);
        */
        app = this;

        startProfilingIfStarted();
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

    public void startKernelLog() {
        Su su = new Su();
        //su.prepare();
        //su.execSu("/data/local/sprofiler 7");   // clear logs
        Su.executeSuOnce("/data/local/sprofiler 1", 30000);
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

    private void startLogging() {
        Log.i(TAG, "startLogging()");
        if (state != State.stopped) {
            Log.i(TAG, "startLogging() : not stopped");
            return;
        }
        state = State.started;
        prefMgr.setAppState(State.started.name());
        // start service
        //Intent startServiceIntent = new Intent(this, LSPService.class);

        resumeLogging();
        LSPLog.onTextMsg("startLogging()");
        startKernelLog();
    }

    public void resumeLogging() {
        showToast("resumeLogging()");
        Log.i(TAG, "resumeLogging()");

        state = State.resumed;
        try {
            lspLog.resumeLogging();
            receiverManager.registerReceivers();
            klogAlarmManager.setFirstAlarmIfNotSetted();
        }catch (Exception ex) {
            FileLogWritter.writeException(ex);
        }

        try {
            Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            LSPLog.onTextMsg("resumeLogging ds=" + display.getState());
        } catch (Exception ex) {
            FileLogWritter.writeException(ex);
        }
    }

    public void pauseLogging(String msg) {
        //showToast("pauseLogging()");
        Log.i(TAG, "pauseLogging()");
        LSPLog.onTextMsg("pauseLogging() "+msg);
        klogAlarmManager.clearAlarm();
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
        klogAlarmManager.clearAlarm();
        if (state == State.resumed)
            pauseLogging("stopLogging() called");

        if (state != State.paused) {
            Log.i(TAG, "stopLogging() : not paused");
            return;
        }

        state = State.stopped;
        prefMgr.setAppState(State.stopped.name());

        //stopService(new Intent(this, LSPService.class));
    }

    public void doWearReport(String mac) {
        Log.i(TAG, "doWearReport, pauseLogging and start service");
        Intent startServiceIntent = new Intent(this, WSLPReportService.class);
        startServiceIntent.putExtra("serverMac", mac);
        startServiceIntent.putExtra("resumeWhenFinished", true);
        pauseLogging("doWearReport called");
        startService(startServiceIntent);
    }

    public void doKLogBackup() {
        ReportItem item = new ReportItem();
        try {
            if (reporter.isKlogEnabled()) {
                reporter.requestReportToDaemon(item);
                reporter.waitForKlogFinish(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }
    }

    public void resetLog() {
        try {
            Su.executeSuOnce("/data/local/sprofiler 7", 5000);
            dbHandler.resetDB();
        } catch (Exception ex) {
            LSPLog.onException(ex);
        }
    }

    @Override
    public void onTerminate() {
        if (state == State.resumed)
            FileLogWritter.writeString("ERR APP onTerminate() state " + state);
        super.onTerminate();
        receiverManager.unregisterReceivers();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "onLowMemory()");
        if (state == State.resumed)
            FileLogWritter.writeString("APP : onLowMemory() state " + state);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigureationChanged()");
    }

    static Toast toast;
    public void showToast(String msg) {
        try {
            toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            toast.show();
            toastWl.acquire(2500);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}