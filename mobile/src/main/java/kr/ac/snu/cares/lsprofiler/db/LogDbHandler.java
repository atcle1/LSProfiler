package kr.ac.snu.cares.lsprofiler.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.util.FileLogWritter;

/**
 * Created by summer on 3/28/15.
 */
public class LogDbHandler {
    public static final String TAG = LogDbHandler.class.getSimpleName();
    private Context context;
    private SQLiteDatabase db;
    private LogDbHelper helper;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

    private SQLiteStatement InsertLogdbStmt;
    private SQLiteStatement InsertLogdbStmt_wt;
    private SQLiteStatement InsertLogdbStmt2;

    @Override
    public void finalize() {
        if (db != null && db.isOpen())
            db.close();
    }

    public LogDbHandler(Context context) {
        this.context = context;
        open();
    }

    private void prepareStatement() {
        //String sql = "INSERT INTO logdb(i_datetime, t_log) " +
        //      "VALUES (strftime('%s', 'now', 'localtime'), ?)";
        try {
            String sql = "INSERT INTO logdb(t_datetime, t_log) " +
                    "VALUES (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime'), ?)";
            InsertLogdbStmt = db.compileStatement(sql);
            sql = "INSERT INTO logdb(t_datetime, t_log) " +
                    "VALUES (?, ?)";
            InsertLogdbStmt_wt = db.compileStatement(sql);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            String sql2 = "INSERT INTO logdb2(t_log) " +
                    "VALUES (?)";
            InsertLogdbStmt2 = db.compileStatement(sql2);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void closePrepareStatement() {
        if (InsertLogdbStmt != null) {
            InsertLogdbStmt.close();
        }
        if (InsertLogdbStmt2 != null) {
            InsertLogdbStmt2.close();
        }
    }

    public void close() {
        closePrepareStatement();
        if (helper != null)
            helper.close();
    }
    public void open() {
        try {
            helper = new LogDbHelper(context);
            db = helper.getWritableDatabase();
            prepareStatement();
        } catch(Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }
    }

    public long insert(int a, String b) {
        // example code
        ContentValues values = new ContentValues();
        values.put("col_a", a);
        values.put("col_b", b);
        int id = (int)db.insert("dbname", null, values);
        if (id == -1)
            Log.i(TAG, "insert error");
        return id;
    }

    synchronized public int writeLog(String msg)
    {
        if (msg == null)
            return -1;
        try {
            if (InsertLogdbStmt == null) {
                FileLogWritter.writeString("if (InsertLogdbStmt == null) {");
                open();
            }
            InsertLogdbStmt.bindString(1, msg);
            InsertLogdbStmt.execute();
            InsertLogdbStmt.clearBindings();
        } catch (Exception ex) {
            ex.printStackTrace();
            FileLogWritter.writeString(ex.getLocalizedMessage());
        }
        return 0;
    }

    synchronized public int writeLog(String timestamp, String msg)
    {
        if (msg == null)
            return -1;
        try {
            if (InsertLogdbStmt == null) {
                FileLogWritter.writeString("if (InsertLogdbStmt == null) {");
                open();
            }
            InsertLogdbStmt_wt.bindString(1, timestamp);
            InsertLogdbStmt_wt.bindString(2, msg);
            InsertLogdbStmt_wt.execute();
            InsertLogdbStmt_wt.clearBindings();
        } catch (Exception ex) {
            ex.printStackTrace();
            FileLogWritter.writeString(ex.getLocalizedMessage());
        }
        return 0;
    }

    synchronized public int writeLog2(String msg)
    {
        if (msg == null)
            return -1;
        try {
            if (InsertLogdbStmt2 == null) {
                FileLogWritter.writeString("if (InsertLogdbStmt == null) {");
                open();
            }
            InsertLogdbStmt2.bindString(1, msg);
            InsertLogdbStmt2.execute();
            InsertLogdbStmt2.clearBindings();
        }catch (SQLiteConstraintException ex2){

        } catch (Exception ex) {
            ex.printStackTrace();
            FileLogWritter.writeString(ex.getLocalizedMessage());
        }
        return 0;
    }

    public int writeLogs(ArrayList<String> msgList)
    {
        //db.beginTransactionNonExclusive();
        db.beginTransaction();
        for (int i = 0; i < msgList.size(); i++)
        {
            InsertLogdbStmt.bindString(1, msgList.get(i));
            InsertLogdbStmt.execute();
            InsertLogdbStmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        return 0;
    }

    public void printLog()
    {
        /*
        Cursor cursor=db.rawQuery(
                "SELECT idx, datetime(i_datetime, 'unixepoch', 'localtime'), t_log " +
                "FROM logdb", null);
        */
        Cursor cursor=db.rawQuery(
                "SELECT idx, t_datetime, t_log " +
                        "FROM logdb", null);
        if(cursor!=null){
            while(cursor.moveToNext()) {
                Log.i(TAG, cursor.getInt(0) + " " + cursor.getString(1) + " " + cursor.getString(2));
            }
        } else {
            Log.i(TAG, "printLog() null");
        }
        cursor.close();
    }

    public void printLog2()
    {
        /*
        Cursor cursor=db.rawQuery(
                "SELECT idx, datetime(i_datetime, 'unixepoch', 'localtime'), t_log " +
                "FROM logdb", null);
        */
        Cursor cursor=db.rawQuery(
                "SELECT idx, t_log " +
                        "FROM logdb2", null);
        if(cursor!=null){
            while(cursor.moveToNext()) {
                Log.i(TAG, cursor.getInt(0) + " " + cursor.getString(1));
            }

        } else {
            Log.i(TAG, "printLog() null");
        }
        cursor.close();
    }

    public boolean backupDB(String backupFilePath){
        Log.i(TAG, "backupDB()");
        ReadableByteChannel readableByteChannel;
        try {
            if (db == null || helper == null || !db.isOpen()) {
                Log.i(TAG, "resetDB() db == null || helper == null");
                open();
            }

            String pathStr = helper.getWritableDatabase().getPath();
            FileInputStream fis=new FileInputStream(new File(pathStr));
            FileOutputStream fos=new FileOutputStream(new File(backupFilePath));
            readableByteChannel = fis.getChannel();
            if (readableByteChannel == null) {
                Log.i(TAG, "readableByteChannel == null");
                return false;
            }
            fos.getChannel().transferFrom(readableByteChannel, 0, 32 * 1024 * 1024);
            fos.close();
            fis.close();

            Log.i(TAG, "backupDB() end, test");
            File f1 = new File(pathStr);
            File f2 = new File(backupFilePath);
            if (!f1.exists() || !f2.exists()) {
                Log.i(TAG, "!f1.exists() || !f2.exists()");
                return false;
            }
            if (f2.length() == 0) {
                Log.i(TAG, "f2.length() == 0");
                return false;
            }
            if (f1.length() != f2.length()) {
                Log.i(TAG, "f1.length() != f2.length()");
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            FileLogWritter.WriteException(ex);
        }
        return false;
    }

    public void restoreDB(String backupFilePath) {
        try {
            String pathStr = helper.getWritableDatabase().getPath();
            close();
            FileInputStream fis=new FileInputStream(new File(backupFilePath));
            FileOutputStream fos=new FileOutputStream(new File(pathStr));
            fos.getChannel().transferFrom(fis.getChannel(), 0, 32 * 1024 * 1024);
            fos.close();
            fis.close();
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetDB() {
        if (db == null) {
            Log.i(TAG, "resetDB() db == null");
            open();
        }
        helper.resetDB(db);
    }
}