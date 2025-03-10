package kr.ac.snu.cares.lsprofiler;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.email.Mail;
import kr.ac.snu.cares.lsprofiler.klog.KlogAlarmManager;
import kr.ac.snu.cares.lsprofiler.pref.LSPPreferenceManager;
import kr.ac.snu.cares.lsprofiler.receivers.LocationTracer;
import kr.ac.snu.cares.lsprofiler.receivers.ReceiverManager;
import kr.ac.snu.cares.lsprofiler.resolvers.BtLogResolver;
import kr.ac.snu.cares.lsprofiler.resolvers.FitnessResolver;
import kr.ac.snu.cares.lsprofiler.service.LSPNotificationService;
import kr.ac.snu.cares.lsprofiler.service.LSPService;
import kr.ac.snu.cares.lsprofiler.util.DeviceID;
import kr.ac.snu.cares.lsprofiler.util.FileLogWritter;
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

    private static boolean wearLoggingEnabled = true;
    public boolean getWearLoggingEnabled() {
        return wearLoggingEnabled;
    }
    public void setWearLoggingEnabled(boolean bEnabled) {
        wearLoggingEnabled = bEnabled;
        prefMgr.setWearEnabled(bEnabled);
    }

    public static boolean fitnessEnabled = false;

    //private DaemonClient clientHandler;
    //HandlerThread daemonClientThread;

    private LogDbHandler dbHandler;
    private LSPPreferenceManager prefMgr;

    private LSPLog lspLog;

    public static final boolean bLocationTracerEnabled = false;

    private ReceiverManager receiverManager;
    private LocationTracer locationTracer;
    private LSPNotificationService notificationService;
    private LSPAlarmManager alarmManager;
    private KlogAlarmManager klogAlarmManager;

    private FitnessResolver fitnessResolver;

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

    private boolean bRoot = false;
    public static boolean bStopAfterReport = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        Context context = getApplicationContext();
        cr = getContentResolver();
        prefMgr = LSPPreferenceManager.getInstance(context);
        wearLoggingEnabled = prefMgr.getWearEnabled();
        int alarmTime[] = prefMgr.getAlarmTime();
        boolean bAlarmEnabled = prefMgr.getAlarmEnabled();

        dbHandler = new LogDbHandler(context);
        lspLog = new LSPLog(context);

        receiverManager = new ReceiverManager(context);
        locationTracer = new LocationTracer(context);
        alarmManager = LSPAlarmManager.getInstance(context);
        alarmManager.setAlarmTime(alarmTime[0], alarmTime[1]);
        alarmManager.setAlarmEnabled(bAlarmEnabled);

        klogAlarmManager = KlogAlarmManager.getInstance(context);

        pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        reportWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "doReport");

        deviceID = prefMgr.getDeviceID();
        if (deviceID.equals("")) {
            deviceID = DeviceID.getDeviceID(this);
            prefMgr.setDeviceID(deviceID);
        }

        reporter = new LSPReporter(this);

        fitnessResolver = new FitnessResolver(this, null);

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
        // watchDog = new WatchDog();
        // bRoot = Su.isRooted();
        Log.i(TAG, "onCreate() end");

        startProfilingIfStarted();
    }

    public LogDbHandler getDbHandler() {
        return dbHandler;
    }
    public static LSPApplication getInstance() { return LSPApplication.app; }
    public FitnessResolver getFitnessResolver() {return fitnessResolver; }
    public boolean isRoot() { return bRoot; }

    public void startProfiling() {
        Log.i(TAG, "startProfiling()");
        prefMgr.setLoggingState("start");
        //connection.sendMessage("/LSP/WINFO", "MAC");
        if (wearLoggingEnabled) {
            connection.sendMessage("/LSP/CONTROL", "START");
            Log.i(TAG, "wear enabled");
        }
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
        Su.executeSuOnce("/data/local/sprofiler 1", 5000);
    }

    public void stopKernelLog() {
        Su su = new Su();
        Su.executeSuOnce("/data/local/sprofiler 2", 5000);
    }

    public void startProfilingIfStarted() {
        String savedState = prefMgr.getLoggingState();
        if (savedState.equals("start") && state == State.stopped) {
            Log.i(TAG, "startProfilingIfStarted() start profiling..." + savedState);
            startProfiling();
        } else {
            Log.i(TAG, "startProfilingIfStarted() not start");
            alarmManager.clearAlarm();
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

        resumeLogging();
        startKernelLog();
    }

    private final Uri SCREEN_OFF_TIMEOUT_URI
            = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT);
    ContentResolver cr = null;
    SettingsObserver observer = null;

    public void resumeLogging() {
        showToast("resumeLogging()");
        Log.i(TAG, "resumeLogging()");
        int timeout = 0;

        try {
            timeout = Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception ex) {
            lspLog.onException(ex);
        }
        state = State.resumed;
        try {
            lspLog.resumeLogging();
            alarmManager.setAlarmEnabled(prefMgr.getAlarmEnabled());
            alarmManager.setFirstAlarmIfNotSetted();
            klogAlarmManager.setFirstAlarmIfNotSetted();
            receiverManager.registerReceivers();
            if (bLocationTracerEnabled)
                locationTracer.startTrace();
            if (handler == null)
                handler = new Handler();
            observer = new SettingsObserver(handler);
            if (cr == null)
                cr = getContentResolver();
            cr.registerContentObserver(SCREEN_OFF_TIMEOUT_URI, true, observer);
            FileLogWritter.writeString("resumeLogging()");
        }catch (Exception ex) {
            FileLogWritter.writeException(ex);
        }

        try {
            LSPNotificationService.startSelf(this);
            Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            LSPLog.onTextMsg("resumeLogging ds=" + display.getState());
            lspLog.onScreenTimeoutChanged(timeout);
            BtLogResolver.enableBtLog();
        } catch (Exception ex) {
            FileLogWritter.writeException(ex);
        }
    }

    public void pauseLogging(String msg) {
        showToast("pauseLogging()");
        Log.i(TAG, "pauseLogging()");
        FileLogWritter.writeString("pauseLogging()");
        klogAlarmManager.clearAlarm();
        if (state != State.resumed) {
            Log.i(TAG, "pauseLogging() : not resumed");
            return;
        }
        state = State.paused;
        receiverManager.unregisterReceivers();
        locationTracer.stopTrace();
        LSPNotificationService.stopSelf(this);
        LSPLog.onTextMsg("pauseLogging " + msg);
        BtLogResolver.disableBtLog();
        if (cr != null) {
            cr.unregisterContentObserver(observer);
        }
        lspLog.pauseLogging();
    }

    public void stopLogging() {
        alarmManager.clearAlarm();
        klogAlarmManager.clearAlarm();
        if (state == State.resumed)
            pauseLogging("stopLogging() called");

        if (state != State.paused) {
            Log.i(TAG, "stopLogging() : not paused");
            return;
        }

        state = State.stopped;
        prefMgr.setAppState(State.stopped.name());

        stopService(new Intent(this, LSPService.class));
        stopKernelLog();
    }

    public LSPReporter getReporter() {
        return reporter;
    }

    public void doReport() {
        reportWl.acquire(1000 * 600);
        showToast("doReport()");
        pauseLogging("called doReport");
        LSPLog.onTextMsgForce("doReport call");
        reporter.doReport();    // send mail is processed by asynctask after doReport with own WL.
                                // resume logging when sendMail end
        LSPLog.onTextMsgForce("doReport end");
        //app.resumeLogging();
        reportWl.release();
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
        Log.i(TAG, "onLowMemory()");
        super.onLowMemory();
        if (state == State.resumed)
            FileLogWritter.writeString("APP : onLowMemory() state " + state);
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

    public void setAlarmTime(int hour, int min) {
        prefMgr.setAlarmTime(hour, min);
        alarmManager.setAlarmTime(hour, min);
    }

    public int[] getAlarmTime() {
        return prefMgr.getAlarmTime();
    }

    public void setAlamEnabled(boolean bEnabled) {
        prefMgr.setAlarmEnabled(bEnabled);
        alarmManager.setAlarmEnabled(bEnabled);;
        if (bEnabled && state == State.resumed) {
            alarmManager.setFirstAlarmIfNotSetted();
        }
    }

    public boolean getAlarmEnabled() {
        return prefMgr.getAlarmEnabled();
    }

    class SettingsObserver extends ContentObserver{
        private final Uri SCREEN_OFF_TIMEOUT_URI
                = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT);

        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            if (cr == null) return;
            /* LSP start */
            int timeout = Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, 30);
            LSPLog.onScreenTimeoutChanged(timeout);
            /* LSP end */

        }
    }

    public void sendPing() {
        connection.sendPing(1000);
    }
}