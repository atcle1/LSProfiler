package kr.ac.snu.cares.lsprofiler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import kr.ac.snu.cares.lsprofiler.daemon.DaemonClient;
import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.email.Mail;
import kr.ac.snu.cares.lsprofiler.pref.LSPPreferenceManager;
import kr.ac.snu.cares.lsprofiler.receivers.LocationTracer;
import kr.ac.snu.cares.lsprofiler.receivers.ReceiverManager;
import kr.ac.snu.cares.lsprofiler.service.LSPNotificationService;
import kr.ac.snu.cares.lsprofiler.service.LSPService;
import kr.ac.snu.cares.lsprofiler.util.DeviceID;
import kr.ac.snu.cares.lsprofiler.util.MyConsoleExe;
import kr.ac.snu.cares.lsprofiler.util.NetworkUtil;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;

/**
 * Created by summer on 3/28/15.
 */
public class LSPApplication extends Application {
    public static final String TAG = LSPApplication.class.getSimpleName();
    public enum State {stopped, started, resumed, paused, error};
    public State state = State.stopped;

    private DaemonClient clientHandler;
    HandlerThread daemonClientThread;


    private LogDbHandler dbHandler;
    private LSPPreferenceManager prefMgr;

    private LSPLog lspLog;

    private ReceiverManager receiverManager;
    private LocationTracer locationTracker;
    private LSPNotificationService notificationService;
    private LSPAlarmManager alarmManager;

    private Mail mail;
    private PowerManager pm;
    private PowerManager.WakeLock wl;

    private String deviceID;

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
        pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "send report");

        receiverManager.registerReceivers();

        deviceID = prefMgr.getDeviceID();
        if (deviceID.equals("")) {
            deviceID = DeviceID.getDeviceID(this);
            prefMgr.setDeviceID(deviceID);
        }

        daemonClientThread = new HandlerThread("daemon client thread");
        daemonClientThread.start();
        clientHandler = new DaemonClient(daemonClientThread.getLooper());
        clientHandler.init(this);
    }

    public LogDbHandler getDbHandler() {
        return dbHandler;
    }


    public void startLogging() {
        if (state != State.stopped) {
            Log.i(TAG, "startLogging() : not stopped");
            return;
        }
        state = State.started;
        // start service
        Intent startServiceIntent = new Intent(this, LSPService.class);
        //startServiceIntent.putExtra("setting", setting);
        startServiceIntent.putExtra("first_start", true);
        startService(startServiceIntent);

        alarmManager.setFirstAlarm();
        resumeLogging();
    }
    public void resumeLogging() {
        state = State.resumed;
        lspLog.resumeLogging();
        receiverManager.registerReceivers();
        locationTracker.startTrace();
        LSPNotificationService.startSelf(this);
    }

    public void pauseLogging() {
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
        if (state == State.resumed)
            pauseLogging();

        if (state != State.paused) {
            Log.i(TAG, "stopLogging() : not paused");
            return;
        }

        state = State.stopped;
        alarmManager.clearAlarm();
        stopService(new Intent(this, LSPService.class));
    }

    public static final String COLLECT_PATH = "/sdcard/LSP/";
    public static final String BACKUP_BASE_PATH = "/sdcard/LSP_backup/";

    public void collectReport(ReportItem item) {
        // mkdir
        File baseDir = new File(COLLECT_PATH);
        if(!baseDir.exists())
            baseDir.mkdirs();

        // copy db
        dbHandler.backupDB(COLLECT_PATH + item.reportDateString + ".db");
        // reset db
        dbHandler.resetDB();

        clientHandler.requestCollectLog();

        File collectDir = new File(COLLECT_PATH);
        File[] logFileArray = collectDir.listFiles();

        if (logFileArray == null)
            return;
        item.fileList.addAll(Arrays.asList(logFileArray));
     }

    public void sendReport(ReportItem item) {
        SendMailAsyncTask sendMailAsyncTask = new SendMailAsyncTask();
        sendMailAsyncTask.title = "LSP report from " + deviceID;
        sendMailAsyncTask.message = "deviceID";

        // get file path
        //sendMailAsyncTask.files = new String []{backupDbPath};
        sendMailAsyncTask.files = new String [item.fileList.size()];
        for (int i = 0; i < item.fileList.size(); i++) {
            sendMailAsyncTask.files[i] = item.fileList.get(i).getAbsolutePath();
        }

        sendMailAsyncTask.wl = this.wl;
        Log.i(TAG, "send "+item.fileList.size()+" items.");
        sendMailAsyncTask.reportItem = item;
        sendMailAsyncTask.execute();
    }

    public void backReport(ReportItem item) {
        File baseDir = new File(item.backupPath);
        if(!baseDir.exists())
            baseDir.mkdirs();

        MyConsoleExe exe = new MyConsoleExe();
        StringBuilder result = new StringBuilder();

       // exe.exec("cp -r "+COLLECT_PATH+"* "+item.backupPath, result, false);
        Log.i(TAG, "backupReport() : " + result);
    }

    public void doReport() {
        Log.i(TAG, "doReport()");
        pauseLogging();
        ReportItem item = new ReportItem();

        // collect reports...
        collectReport(item);

        if (NetworkUtil.getConnectivityStatus(this) != NetworkUtil.TYPE_WIFI) {
            // not wifi
            alarmManager.clearAlarm();
            alarmManager.setNextAlarmAfter(60);

        } else {
            // send report via email
            sendReport(item);
        }

        // backup report
        // backReport(item);
        resumeLogging();
    }

    private class SendMailAsyncTask extends AsyncTask<Void, Void, Void> {
        public String title = "";
        public String message = "";
        public String []files = null;
        public PowerManager.WakeLock wl;
        public ReportItem reportItem = null;

        @Override
        protected Void doInBackground(Void... params) {
            wl.acquire();
            try {
                int result = Mail.sendReport(title, message, files);
                if (result != 0) {
                    Log.i(TAG, "send mail failed!");
                }

                // make backup dir
                File backupDir = new File(reportItem.backupPath);
                if(!backupDir.exists())
                    backupDir.mkdirs();

                // move
                for (int i = 0; i < reportItem.fileList.size(); i++) {
                    File origFile = reportItem.fileList.get(i);
                    File destFile = new File(reportItem.backupPath + origFile.getName());
                    origFile.renameTo(destFile);
                    Log.i(TAG, "rename " + origFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
            wl.release();
            return null;
        }

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
}