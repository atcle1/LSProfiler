package kr.ac.snu.cares.lsprofiler.util;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by summer on 15. 5. 18.
 */
public class FileLogWritter {
    public static final String TAG = FileLogWritter.class.getSimpleName();
    public static final String LOG_PATH = "/sdcard/LSP/";
    public static String LOG_FILE_NAME = null;
    private static File logfile;
    private static BufferedWriter bufferWritter;

    public static void makeFileLogFile() {
        try {
            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            if (LOG_FILE_NAME == null) {
                // just, once
                LOG_FILE_NAME = transFormat.format(new Date()) + ".filelog.txt";
            }
            logfile = new File(LOG_PATH + LOG_FILE_NAME);

            if (!logfile.exists()) {
                // 1. check dirs
                File dir = new File(LOG_PATH);
                if (!dir.exists())
                    dir.mkdirs();

                // 2. make new logfile
                LOG_FILE_NAME = transFormat.format(new Date()) + ".filelog.txt";
                logfile = new File(LOG_PATH + LOG_FILE_NAME);
                logfile.createNewFile();
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void writeString(String msg) {
        try {
            Log.e(TAG, "FileLogWritter : " + msg);

            makeFileLogFile();

            //logfile = new File
            if (logfile == null) {
                return;
            }

            FileWriter fw = new FileWriter(logfile, true);
            BufferedWriter bufferWritter = new BufferedWriter(fw);
            bufferWritter.write(Util.getTimeStringFromSystemMillis((System.currentTimeMillis())) + "\n");
            bufferWritter.write(msg + "\n");
            bufferWritter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeException(Exception ex) {
        try {
            ex.printStackTrace();
            String stackTrace = Util.getStackTrace(ex);
            writeString(stackTrace + "\n" + ex.getLocalizedMessage());
        } catch (Exception ex2) {
            ex.printStackTrace();
        }
    }
}
