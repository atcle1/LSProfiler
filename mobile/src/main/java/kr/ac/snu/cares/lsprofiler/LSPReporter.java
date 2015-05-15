package kr.ac.snu.cares.lsprofiler;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.email.Mail;
import kr.ac.snu.cares.lsprofiler.util.MyConsoleExe;
import kr.ac.snu.cares.lsprofiler.util.NetworkUtil;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;
import kr.ac.snu.cares.lsprofiler.util.Su;
import kr.ac.snu.cares.lsprofiler.wear.LSPConnection;
import kr.ac.snu.cares.lsprofiler.wear.LSPReportServer;

/**
 * Created by summer on 4/24/15.
 */
public class LSPReporter {
    public LSPApplication app;
    public static final String TAG = LSPReporter.class.getSimpleName();
    public static final String COLLECT_MOBILE_PATH = "/sdcard/LSP/";
    public static final String COLLECT_WEAR_PATH = "/sdcard/LSPW/";
    public static final String BACKUP_BASE_PATH = "/sdcard/LSP_backup/";
    public static final int COLLECT_WATCH_REPORT_TIME_LIMIT_S = 500;

    private LogDbHandler dbHandler;
    private PowerManager pm;
    private PowerManager.WakeLock sendWl;

    public LSPReporter(LSPApplication app)
    {
        this.app = app;
        dbHandler = app.getDbHandler();
        pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        sendWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "send report");
    }

    public void requestReportToDaemon(ReportItem item) {
        Log.i(TAG, "requestReportToDaemon");
        //DaemonStarter.startForReport(COLLECT_MOBILE_PATH, item.reportDateString + ".klog");
        Su su = new Su();
        su.prepare();
        su.execSu("/data/local/sprofiler 3 "+ COLLECT_MOBILE_PATH +" "+ item.reportDateString + ".klog");
        su.execSu("/data/local/sprofiler 7");   // clear logs
        su.stopSu(3000);    // 30s limit
    }

    public boolean isKlogEnabled() {
        File f = new File("/data/local/sprofiler");
        try {
            if (!f.exists()) {
                Log.i(TAG, "sprofiler not founded!");
                LSPLog.onTextMsg("sprofiler not founded!");
                return false;
            }
            if (!Su.isRooted()) {
                Log.i(TAG, "not rooted! but try report!");
                LSPLog.onTextMsg("not rooted! but try report!");
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;

    }

    public void waitForKlogFinish(ReportItem item) throws Exception{
        // klog should be finished within 20s.
        File finishFile = new File(COLLECT_MOBILE_PATH + item.reportDateString + ".klog.finish");
        for (int i = 0; i < 20; i++) {
            if (finishFile.exists()) {
                Log.i(TAG, "KLSP finished detected! " + i);
                //finishFile.delete();
                break;
            }
            Thread.sleep(1000);
        }
    }

    public void collectPhoneReport(ReportItem item) {
        try {
            // mkdir
            File baseDir = new File(COLLECT_MOBILE_PATH);
            if (!baseDir.exists())
                baseDir.mkdirs();

            // copy db
            boolean backupDbSuccess = dbHandler.backupDB(COLLECT_MOBILE_PATH + item.reportDateString + ".db");
            // reset db
            if (backupDbSuccess)
                dbHandler.resetDB();
            else {
                LSPLog.onTextMsgForce(TAG + "backupdb failed");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }

        try {
            if (isKlogEnabled()) {
                //listing log files...
                requestReportToDaemon(item);
                waitForKlogFinish(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }
    }

    public void sendReportByAsyncTask(ReportItem item) {
        SendMailAsyncTask sendMailAsyncTask = new SendMailAsyncTask();
        sendMailAsyncTask.title = "LSP report from " + app.deviceID;
        sendMailAsyncTask.message = "writting... " + Calendar.getInstance().getTime();

        // get file path
        //sendMailAsyncTask.files = new String []{backupDbPath};
        sendMailAsyncTask.files = new String [item.fileList.size()];
        for (int i = 0; i < item.fileList.size(); i++) {
            sendMailAsyncTask.files[i] = item.fileList.get(i).getAbsolutePath();
        }

        sendMailAsyncTask.wl = this.sendWl;
        Log.i(TAG, "sendReportByAsyncTask() start " + item.fileList.size() + " items.");
        sendMailAsyncTask.reportItem = item;
        sendMailAsyncTask.execute();
    }

    public void backReport(ReportItem item) {
        File baseDir = new File(item.backupDir);
        if(!baseDir.exists())
            baseDir.mkdirs();

        MyConsoleExe exe = new MyConsoleExe();
        StringBuilder result = new StringBuilder();

        // exe.exec("cp -r "+COLLECT_MOBILE_PATH+"* "+item.backupDir, result, false);
        Log.i(TAG, "backupReport() : " + result);
    }

    public boolean collectWatchReport(int timeLimits) {
        LSPReportServer reportServer = new LSPReportServer();

        LSPConnection connection = app.getLSPConnection();
        if (connection == null ) {
            return false;
        }
        connection.connect();

        String btmac = NetworkUtil.getBluetoothAddress();
        if (btmac == null)
            return false;

        reportServer.start();
        try {
            int i = 0;
            while (reportServer.isAlive() == false && i++ < 100) {
                Thread.sleep(100);
            }
            Log.i(TAG, "send report message " + NetworkUtil.getBluetoothAddress());
            //connection.sendMessage("/LSP/CONTROL", "STOP");
            connection.sendMessage("/LSP/CONTROL", "REPORT "+NetworkUtil.getBluetoothAddress());
        }catch(Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
            return false;
        }

        if (true) {
            try {
                int i;
                Log.i(TAG, "reportServr join start...");
                for (i = 0; i < timeLimits; i++) {
                    reportServer.join(1000);
                    //Log.i(TAG, "reportServer.join " + (i + 1));
                }
                if (i == 500) {
                    Log.e(TAG, "reportServer join timeout, interrupt()");
                    reportServer.interrupt();
                    return false;
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                LSPLog.onException(ex);
                return false;
            }
        }
        return true;
    }

    class WearCollectThread extends Thread {
        @Override
        public void run() {
            super.run();
            collectWatchReport(COLLECT_WATCH_REPORT_TIME_LIMIT_S);
        }
    }


    public void doReport() {
        Log.i(TAG, "doReport() start");
        try {

            boolean bCollectWear = false;
            ReportItem item = new ReportItem();
            LSPApplication app = LSPApplication.getInstance();

            WearCollectThread wearCollectThread = null;
            try {
                if (LSPApplication.getInstance().wearLoggingEnabled) {

                    if (!app.getLSPConnection().isWearConnected()) {
                        app.getLSPConnection().connect();
                        Thread.sleep(100);
                    }
                    if (app.getLSPConnection().isWearConnected()) {
                        bCollectWear = true;
                    } else {
                        LSPLog.onTextMsgForce("wearlogging is enabled, but isn't connected!");
                        Log.i(TAG, "wearlogging is enabled, but isn't connected!");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                bCollectWear = false;
            }

            // collect reports from wear using thread...
            if (bCollectWear) {
                wearCollectThread = new WearCollectThread();
                wearCollectThread.start();
                Log.i(TAG, "wearCollectThread run() called");
            }

            // collect mobile logs....
            if (true) {
                collectPhoneReport(item);
                Log.i(TAG, "collectphoneReport() ended");
            }

            // join for wearCollect thread
            if (bCollectWear) {
                    int i;
                    for (i = 0; i < 500; i++)
                        wearCollectThread.join(1000);
                    Log.i(TAG, "wear collect finished " + i);
                    if (i >= 500) {
                        wearCollectThread.interrupt();
                        LSPLog.onTextMsgForce(TAG + " wearCollectThread not finished within 500s, interrupt()");
                    }
            }

            // listing log files...
            File collectMobileDir = new File(COLLECT_MOBILE_PATH);
            File[] logFileArray = collectMobileDir.listFiles();
            File collectWearDir;
            File[] logFileArray2 = null;

            if (bCollectWear) {
                collectWearDir = new File(COLLECT_WEAR_PATH);
                logFileArray2 = collectWearDir.listFiles();
            }

            if (logFileArray == null && logFileArray2 == null)
                return;

                if (logFileArray != null)
                    item.fileList.addAll(Arrays.asList(logFileArray));
                if (logFileArray2 != null)
                    item.fileList.addAll(Arrays.asList(logFileArray2));

                if (NetworkUtil.getConnectivityStatus(app) != NetworkUtil.TYPE_WIFI) {
                    // not wifi
                    Log.i(TAG, "doReport() but not connected WIFI");
                    LSPLog.onTextMsg("try report, NOT WIFI");
                    app.getAlarmManager().clearAlarm();
                    app.getAlarmManager().setNextAlarmAfter(1000 * 60 * 60 * 2); // 2 hour later...

                } else {
                    // send report via email
                    sendReportByAsyncTask(item);
                }
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        // backup report
        // backReport(item);
        Log.i(TAG, "doReport() end");
    }

    private class SendMailAsyncTask extends AsyncTask<Void, Void, Void> {
        public String title = "";
        public String message = "";
        public String []files = null;
        public String []send_files = null;
        public PowerManager.WakeLock wl;
        public ReportItem reportItem = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                wl.acquire();
                if (files == null || files.length == 0) {
                    wl.release();
                    Log.e(TAG, "SendMailAsyncTask no files!");
                    return null;
                }
                send_files = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    String filePath = files[i];
                    File f = new File(filePath);
                    if (!f.exists() || f.length() > 25 * 1024 * 1024) { // fileSize > 25MB
                        files[i] = "";  //skip file.
                    } else {
                        send_files[i] = files[i];
                    }
                }

                int result = Mail.sendReport(title, message, send_files);
                //int result = -1;

                if (result != 0) {
                    LSPLog.onTextMsgForce("send mail failed! "+Calendar.getInstance().getTime() + " " + reportItem.reportDate);
                    Log.i(TAG, "send mail failed!");
                }

                backupReports(reportItem);
                LSPLog.onTextMsgForce("backupReports end");
            }catch(Exception ex) {
                ex.printStackTrace();
            } finally {
                wl.release();
            }
            return null;
        }
    }

    void backupReports(ReportItem reportItem) {
        try {

            // make backup dir
            File backupDir = new File(reportItem.backupDir);
            if(!backupDir.exists())
                backupDir.mkdirs();

            // move
            for (int i = 0; i < reportItem.fileList.size(); i++) {
                File origFile = reportItem.fileList.get(i);
                File destFile = new File(reportItem.backupDir + origFile.getName());
                origFile.renameTo(destFile);
                Log.i(TAG, "rename " + origFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
