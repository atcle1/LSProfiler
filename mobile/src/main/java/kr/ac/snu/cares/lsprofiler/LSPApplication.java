package kr.ac.snu.cares.lsprofiler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.email.Mail;
import kr.ac.snu.cares.lsprofiler.pref.LSPPreferenceManager;
import kr.ac.snu.cares.lsprofiler.receivers.LocationTracer;
import kr.ac.snu.cares.lsprofiler.receivers.ReceiverManager;
import kr.ac.snu.cares.lsprofiler.service.LSPNotificationService;
import kr.ac.snu.cares.lsprofiler.service.LSPService;
import kr.ac.snu.cares.lsprofiler.util.DeviceID;
import kr.ac.snu.cares.lsprofiler.util.NetworkUtil;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;
import kr.ac.snu.cares.lsprofiler.util.Su;
import kr.ac.snu.cares.lsprofiler.util.WatchDog;
import kr.ac.snu.cares.lsprofiler.wear.LSPConnection;

/**
 * Created by summer on 3/28/15.
 */
public class LSPApplication extends Application {
    public static final String TAG = LSPApplication.class.getSimpleName();
    private static LSPApplication app;
    public enum State {stopped, started, resumed, paused, error};
    public State state = State.stopped;
    private Su su;

    private LSPReporter reporter;

    public boolean wearLoggingEnabled = true;

    //private DaemonClient clientHandler;
    //HandlerThread daemonClientThread;

    private LogDbHandler dbHandler;
    private LSPPreferenceManager prefMgr;

    private LSPLog lspLog;

    private ReceiverManager receiverManager;
    private LocationTracer locationTracker;
    private LSPNotificationService notificationService;
    private LSPAlarmManager alarmManager;

    private LSPConnection connection;

    private Mail mail;
    private PowerManager pm;
    private PowerManager.WakeLock reportWl;

    public String deviceID;

    public LSPAlarmManager getAlarmManager()
    {
        return alarmManager;
    }
    public LSPConnection getLSPConnection() {return connection; }
    private Handler handler;

    private WatchDog watchDog;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        Context context = getApplicationContext();
        dbHandler = new LogDbHandler(context);
        lspLog = new LSPLog(context);
        prefMgr = LSPPreferenceManager.getInstance(context);
        receiverManager = new ReceiverManager(context);
        locationTracker = new LocationTracer(context);
        alarmManager = LSPAlarmManager.getInstance(context);

        pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        reportWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "doReport");

        deviceID = prefMgr.getDeviceID();
        if (deviceID.equals("")) {
            deviceID = DeviceID.getDeviceID(this);
            prefMgr.setDeviceID(deviceID);
        }

        reporter = new LSPReporter(this);

        connection = new LSPConnection(this);
        connection.connect();

        /*
        daemonClientThread = new HandlerThread("daemon client thread");
        daemonClientThread.start();
        clientHandler = new DaemonClient(daemonClientThread.getLooper());
        clientHandler.init(this);
        */
        app = this;

        handler = new Handler();

        watchDog = new WatchDog();

    }

    public LogDbHandler getDbHandler() {
        return dbHandler;
    }
    public static LSPApplication getInstance() { return LSPApplication.app; }

    public void startProfiling() {
        Log.i(TAG, "startProfiling()");
        prefMgr.setLoggingState("start");
        //connection.sendMessage("/LSP/WINFO", "MAC");
        connection.sendMessage("/LSP/CONTROL", "START");
        //connection.sendMessage("/LSP/WINFO", "STATUS");

        startLogging();
    }
    public void stopProfiling() {
        Log.i(TAG, "stopProfiling()");
        prefMgr.setLoggingState("stop");
        connection.sendMessage("/LSP/CONTROL", "STOP");
        stopLogging();
    }

    public void startKernelLog() {
        Su su = new Su();
        //su.prepare();
        //su.execSu("/data/local/sprofiler 7");   // clear logs
        Su.executeOnce("/data/local/sprofiler 1", 30000);
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
        Intent startServiceIntent = new Intent(this, LSPService.class);
        //startServiceIntent.putExtra("setting", setting);
        startServiceIntent.putExtra("first_start", true);
        startService(startServiceIntent);

        alarmManager.setFirstAlarm();

        resumeLogging();
        startKernelLog();
    }

    public void resumeLogging() {
        showToast("resumeLogging()");
        Log.i(TAG, "resumeLogging()");
        LSPLog.onTextMsgForce("resumeLogging() " + Calendar.getInstance().getTime().toString());
        state = State.resumed;
        try {
            lspLog.resumeLogging();
            receiverManager.registerReceivers();
            locationTracker.startTrace();
            LSPNotificationService.startSelf(this);
        }catch (Exception ex) {
            ex.printStackTrace();;
            LSPLog.onTextMsg(ex.getLocalizedMessage());
        }
    }

    public void pauseLogging(String msg) {
        showToast("pauseLogging()");
        Log.i(TAG, "pauseLogging()");
        LSPLog.onTextMsgForce("resumeLogging() " + msg + " " + Calendar.getInstance().getTime().toString());
        if (state != State.resumed) {
            Log.i(TAG, "pauseLogging() : not resumed");
            return;
        }
        state = State.paused;
        receiverManager.unregisterReceivers();
        locationTracker.stopTrace();
        LSPNotificationService.stopSelf(this);
        lspLog.pauseLogging();
    }

    public void stopLogging() {
        LSPLog.onTextMsg("stopLogging()");
        if (state == State.resumed)
            pauseLogging("called stopLogging");

        if (state != State.paused) {
            Log.i(TAG, "stopLogging() : not paused");
            return;
        }

        state = State.stopped;
        prefMgr.setAppState(State.stopped.name());

        alarmManager.clearAlarm();
        stopService(new Intent(this, LSPService.class));
    }

    public LSPReporter getReporter() {
        return reporter;
    }

    public void doReport() {
        reportWl.acquire(1000 * 600);
        showToast("doReport()");
        pauseLogging("called doReport");
        LSPLog.onTextMsgForce("doReport() reporter.doReport() call");
        reporter.doReport();    // send mail is processed by asynctask after doReport with own WL.
        LSPLog.onTextMsgForce("doReport() reporter.doReport() call end");
        app.resumeLogging();
        reportWl.release();
    }

    public void doKLogBackup() {
        ReportItem item = new ReportItem();
        try {
            if (reporter.isKlogEnabled()) {
                reporter.requestReportToDaemon(item);
                reporter.waitForKlogFinish(item);
            }
        }catch(Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }
    }

    @Override
    public void onTerminate() {
        LSPLog.onTextMsgForce("ERR APP onTerminate()");
        showToast("onTerminate()");
        super.onTerminate();
        Log.i(TAG, "onTerminate()");
        receiverManager.unregisterReceivers();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "onLowMemory()");
        LSPLog.onTextMsgForce("ERR APP onLowMemory()");
        showToast("onLowMemory()");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigureationChanged()");
    }

    class ToastMsgThread extends Thread{
        public ToastMsgThread(String msg) {
            this.msg = msg;
        }
        public String msg;

        @Override
        public void run() {
            Toast.makeText(LSPApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
        }
    }
    public void showToast(String msg) {
        if (((Looper.myLooper() != null) && (Looper.myLooper() == Looper.getMainLooper())))
            Toast.makeText(LSPApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
        else
            handler.post(new ToastMsgThread(msg));
    }
}