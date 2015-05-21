package kr.ac.snu.cares.lsprofiler.util;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by summer on 15. 5. 18.
 */
public class FileLogWritter {
    public static final String TAG = FileLogWritter.class.getSimpleName();
    public static final String LOG_PATH = "/sdcard/LSP/";
    public static final String LOG_FILE_NAME = "filelog.txt";
    private static File logfile;
    private static BufferedWriter bufferWritter;

    public static void writeString(String msg) {
        try {
            Log.e(TAG, "FileLogWritter : " + msg);
            logfile = new File(LOG_PATH + LOG_FILE_NAME);
            if (logfile == null) {
                return;
            }
            if (!logfile.exists()) {
                logfile.createNewFile();
            }
            FileWriter fw = new FileWriter(logfile, true);
            BufferedWriter bufferWritter = new BufferedWriter(fw);
            Date date = Calendar.getInstance().getTime();
            bufferWritter.write(date.toString() + "\n");
            bufferWritter.write(msg + "\n");
            bufferWritter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void WriteException(Exception ex) {
        try {
            String stackTrace = Util.getStackTrace(ex);
            writeString(stackTrace + "\n" + ex.getLocalizedMessage());
        } catch (Exception ex2) {
            ex.printStackTrace();
        }
    }
}
