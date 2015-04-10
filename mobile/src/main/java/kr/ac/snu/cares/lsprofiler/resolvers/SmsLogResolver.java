package kr.ac.snu.cares.lsprofiler.resolvers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by summer on 4/10/15.
 */
public class SmsLogResolver {
    ContentResolver resolver;

    public SmsLogResolver(ContentResolver rv){
        resolver= rv;
    }

    public ArrayList<CallLogItem> getSmsLog(Long timeAfter) {
        String timestamp = String.valueOf(timeAfter);
        Uri myMessage = Uri.parse("content://sms/");
        ContentResolver cr = resolver;
        Cursor c = cr.query(myMessage,
                new String[] { "_id", "address", "date", "body", "read", "type" },
                "date >= ?",
                new String[]{timestamp},
                "date desc");

        return getSmsLogs(c);
    }

    public ArrayList<CallLogItem> getSmsLog() {
        Uri myMessage = Uri.parse("content://sms/");
        ContentResolver cr = resolver;
        Cursor c = cr.query(myMessage,
                new String[] { "_id", "address", "date", "body", "read", "type" },
                null,
                null,
                "date desc");

        return getSmsLogs(c);
    }

    public ArrayList<CallLogItem> getSmsLogs(Cursor c) {
        ArrayList<CallLogItem> list = new ArrayList<CallLogItem>();
        CallLogItem item;

        if (c == null) {
            return list;
        }

        try {
            if (c.moveToFirst()) {
                do {
                    item = new CallLogItem();
                    if (c.getString(c.getColumnIndexOrThrow("address")) == null) {
                        c.moveToNext();
                        continue;
                    }

                    String _id = c.getString(c.getColumnIndexOrThrow("_id"))
                            .toString();
                    String Number = c.getString(
                            c.getColumnIndexOrThrow("address")).toString();
                    String dat = c.getString(c.getColumnIndexOrThrow("date"))
                            .toString();
                    String as = dat;
                    String Body = c.getString(c.getColumnIndexOrThrow("body"))
                            .toString();

                    long type = c.getLong(c.getColumnIndexOrThrow("type"));

                    item.logType = CallLogItem.LOGTYPE_SMS;
                    item.number = Number;
                    item.dateMil = c.getLong(c.getColumnIndexOrThrow("date"));

                    item.smsType = (int)type;
                    item.message = Body;
                    //Log.i("sms", _id + " "+item.number +" "+Body+ " t"+type);

                    list.add(item);

                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            c.close();
            e.printStackTrace();
        }
        return list;
    }
}
