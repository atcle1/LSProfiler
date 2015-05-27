package kr.ac.snu.cares.lsprofiler;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.resolvers.DumpsysResolver;
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
        su.execSu("/data/local/sprofiler 3 "+COLLECT_PATH +" "+ item.reportDateString + ".w.klog");
        su.execSu("/data/local/sprofiler 7");   // clear logs
        su.stopSu(2000);
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

    public void waitForKlogFinish(ReportItem item) throws Exception {
        // klog should be finished within 20s.
        File finishFile = new File(COLLECT_PATH + item.reportDateString + ".klog.finish");
        for (int i = 0; i < 20; i++) {
            if (finishFile.exists()) {
                Log.i(TAG, "KLSP finished detected! " + i);
                finishFile.delete();
                break;
            }
            Thread.sleep(1000);
        }
    }

    public void collectReport(ReportItem item) {
        DumpsysResolver dumpsysResolver = new DumpsysResolver();
        try {
            dumpsysResolver.doWriteDumpAsync(COLLECT_PATH + item.reportDateString + ".dump.w.txt");
        } catch (Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }
        try {
            // mkdir
            File baseDir = new File(COLLECT_PATH);
            if(!baseDir.exists())
                baseDir.mkdirs();

            // copy db
            boolean backupDbSuccess = dbHandler.backupDB(COLLECT_PATH + item.reportDateString + ".w.db");
            // reset db
            if (backupDbSuccess)
                dbHandler.resetDB();
            else {
                LSPLog.onTextMsgForce("backup db failed");
                Log.i(TAG, "backup db failed");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }

        try {
            if (isKlogEnabled()) {
                requestReportToDaemon(item);
                waitForKlogFinish(item);
                //listing log files...
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }


        try {
            dumpsysResolver.joinDumpAsync(1000 * 20);
        } catch (Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }


        // all logging finished.
        // listing log files
        File collectDir = new File(COLLECT_PATH);
        File[] logFileArray = collectDir.listFiles();

        if (logFileArray == null)
            return;
        item.fileList.addAll(Arrays.asList(logFileArray));
    }

    public void backupReports(ReportItem reportItem) {
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