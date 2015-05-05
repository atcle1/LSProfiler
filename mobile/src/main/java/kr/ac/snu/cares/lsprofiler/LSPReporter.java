package kr.ac.snu.cares.lsprofiler;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.email.Mail;
import kr.ac.snu.cares.lsprofiler.util.MyConsoleExe;
import kr.ac.snu.cares.lsprofiler.util.NetworkUtil;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;
import kr.ac.snu.cares.lsprofiler.util.Su;

/**
 * Created by summer on 4/24/15.
 */
public class LSPReporter {
    public LSPApplication app;
    public static final String TAG = LSPReporter.class.getSimpleName();
    public static final String COLLECT_PATH = "/sdcard/LSP/";
    public static final String BACKUP_BASE_PATH = "/sdcard/LSP_backup/";
    private LogDbHandler dbHandler;
    private PowerManager pm;
    private PowerManager.WakeLock sendWl;
    private PowerManager.WakeLock reportWl;

    public LSPReporter(LSPApplication app)
    {
        this.app = app;
        dbHandler = app.getDbHandler();
        pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        sendWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "send report");
        reportWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "do report");
    }

    public void requestReportToDaemon(ReportItem item) {
        Log.i(TAG, "requestReportToDaemon");
        //DaemonStarter.startForReport(COLLECT_PATH, item.reportDateString + ".klog");
        Su su = new Su();
        su.prepare();
        su.execSu("/data/local/sprofiler 3 "+COLLECT_PATH +" "+ item.reportDateString + ".klog");
        su.stopSu(1000);
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
    public void collectReport(ReportItem item) {
        // mkdir
        File baseDir = new File(COLLECT_PATH);
        if(!baseDir.exists())
            baseDir.mkdirs();

        // copy db
        dbHandler.backupDB(COLLECT_PATH + item.reportDateString + ".db");
        // reset db
        dbHandler.resetDB();

        //clientHandler.requestCollectLog();
        if (isKlogEnabled()) {
            requestReportToDaemon(item);

            try {
                // klog should be finished within 2s.
                Thread.sleep(1000);
                File finishFile = new File(COLLECT_PATH + item.reportDateString + ".klog.finish");
                for (int i = 0; i < 10; i++) {
                    if (finishFile.exists()) {
                        Log.i(TAG, "KLSP finished detected! " + i);
                        finishFile.delete();
                        break;
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
            }
            ;

            //listing log files...
            try {
                for (int i = 0; i < 3; i++) {
                    File klogFile = new File(COLLECT_PATH + item.reportDateString + ".klog");
                    if (!klogFile.exists()) {
                        Log.i(TAG, "klog not founded, wait");
                        Thread.sleep(500, 0);
                    } else
                        Log.i(TAG, "klog exists " + klogFile.getAbsolutePath());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        File collectDir = new File(COLLECT_PATH);
        File[] logFileArray = collectDir.listFiles();

        if (logFileArray == null)
            return;
        item.fileList.addAll(Arrays.asList(logFileArray));
    }

    public void sendReport(ReportItem item) {
        SendMailAsyncTask sendMailAsyncTask = new SendMailAsyncTask();
        sendMailAsyncTask.title = "LSP report from " + app.deviceID;
        sendMailAsyncTask.message = "deviceID";

        // get file path
        //sendMailAsyncTask.files = new String []{backupDbPath};
        sendMailAsyncTask.files = new String [item.fileList.size()];
        for (int i = 0; i < item.fileList.size(); i++) {
            sendMailAsyncTask.files[i] = item.fileList.get(i).getAbsolutePath();
        }

        sendMailAsyncTask.wl = this.sendWl;
        Log.i(TAG, "send "+item.fileList.size()+" items.");
        sendMailAsyncTask.reportItem = item;
        sendMailAsyncTask.execute();
    }

    public void backReport(ReportItem item) {
        File baseDir = new File(item.backupDir);
        if(!baseDir.exists())
            baseDir.mkdirs();

        MyConsoleExe exe = new MyConsoleExe();
        StringBuilder result = new StringBuilder();

        // exe.exec("cp -r "+COLLECT_PATH+"* "+item.backupDir, result, false);
        Log.i(TAG, "backupReport() : " + result);
    }

    public void doReport() {
        Log.i(TAG, "doReport()");
        try {
            reportWl.acquire();
            app.pauseLogging();
            ReportItem item = new ReportItem();

            // collect reports...
            collectReport(item);

            if (NetworkUtil.getConnectivityStatus(app) != NetworkUtil.TYPE_WIFI) {
                // not wifi
                Log.i(TAG, "doReport() but not connected WIFI");
                LSPLog.onTextMsg("try report, NOT WIFI");
                app.getAlarmManager().clearAlarm();
                app.getAlarmManager().setNextAlarmAfter(1000 * 60 * 60 * 2); // 2 hour later...

            } else {
                // send report via email
                sendReport(item);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        // backup report
        // backReport(item);
        app.resumeLogging();
        reportWl.release();
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
            }catch(Exception ex) {
                ex.printStackTrace();
            }
            wl.release();
            return null;
        }

    }
}
