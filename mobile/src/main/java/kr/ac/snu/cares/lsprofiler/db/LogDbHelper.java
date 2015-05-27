package kr.ac.snu.cares.lsprofiler.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by summer on 3/28/15.
 */
public class LogDbHelper extends SQLiteOpenHelper{
    public SQLiteDatabase db;
    public LogDbHelper(Context context) {
        super(context, "lpdb.db", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        String table = "CREATE TABLE logdb("+
                "idx INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "i_datetime INTEGER," + // as Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC.
                "t_log TEXT" + ")";
                */
        String table = "CREATE TABLE logdb("+
                "idx INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "t_datetime TEXT," + // %Y-%m-%d %H:%M:%f
                "t_log TEXT" + ")";
        db.execSQL(table);

        String table2 = "CREATE TABLE logdb2("+
                "idx INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "t_log TEXT UNIQUE" + ")";
        db.execSQL(table2);
        this.db = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS logdb");
        db.execSQL("DROP TABLE IF EXISTS logdb2");
        onCreate(db);
    }

    public void resetDB(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS logdb");
        db.execSQL("DROP TABLE IF EXISTS logdb2");
        onCreate(db);
    }
}