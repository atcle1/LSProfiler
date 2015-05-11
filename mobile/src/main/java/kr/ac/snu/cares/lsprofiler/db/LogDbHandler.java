package kr.ac.snu.cares.lsprofiler.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
        String sql = "INSERT INTO logdb(i_datetime, t_log) " +
                "VALUES (strftime('%s', 'now', 'localtime'), ?)";
        InsertLogdbStmt = db.compileStatement(sql);
    }
    private void closePrepareStatement() {
        if (InsertLogdbStmt != null)
            InsertLogdbStmt.close();
    }

    public void close() {
        closePrepareStatement();
        if (helper != null)
            helper.close();
    }
    public void open() {
        helper = new LogDbHelper(context);
        db = helper.getWritableDatabase();
        prepareStatement();
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

    public int writeLog(String msg)
    {
        if (msg == null)
            return -1;
        InsertLogdbStmt.bindString(1, msg);
        InsertLogdbStmt.execute();
        InsertLogdbStmt.clearBindings();
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
        Cursor cursor=db.rawQuery(
                "z " +
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
        } catch (Exception e) {
            e.printStackTrace();
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