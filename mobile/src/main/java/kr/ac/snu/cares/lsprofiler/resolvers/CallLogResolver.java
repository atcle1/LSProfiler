package kr.ac.snu.cares.lsprofiler.resolvers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import java.util.ArrayList;

import kr.ac.snu.cares.lsprofiler.util.Util;

/**
 * Created by summer on 4/9/15.
 */
public class CallLogResolver {
    ContentResolver resolver;

    public CallLogResolver(ContentResolver cr){
        resolver=cr;
    }


    public ArrayList<CallLogItem> queryCallLog(long timeAfter){
        String timestamp = String.valueOf(timeAfter);
        /*
        Cursor c = resolver.query(
                android.provider.CallLog.Calls.CONTENT_URI, null,
                null,   null,
                android.provider.CallLog.Calls.DATE + " DESC");
        */

        Cursor c = resolver.query(CallLog.Calls.CONTENT_URI, null,
                CallLog.Calls.DATE + ">= ?", new String[]{timestamp},
                android.provider.CallLog.Calls.DATE + " DESC");
        return getCallLogItemListFromCursor(c);
    }

    public ArrayList<CallLogItem> queryCallLog(){
        String timestamp = String.valueOf(Util.getTodayTimestamp());
        /*
        Cursor c = resolver.query(
                android.provider.CallLog.Calls.CONTENT_URI, null,
                null,   null,
                android.provider.CallLog.Calls.DATE + " DESC");
        */

        Cursor c = resolver.query(CallLog.Calls.CONTENT_URI, null,
                CallLog.Calls.DATE + ">= ?", new String[]{timestamp},
                android.provider.CallLog.Calls.DATE + " DESC");
        return getCallLogItemListFromCursor(c);
    }

    private ArrayList<CallLogItem> getCallLogItemListFromCursor(Cursor c){
        ArrayList<CallLogItem> callLogList=new ArrayList<CallLogItem>();
        int numberColumn = c.getColumnIndex(CallLog.Calls.NUMBER);
        int duraColumn = c.getColumnIndex(android.provider.CallLog.Calls.DURATION);
        int nameColumn = c.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME);
        int typeColumn = c.getColumnIndex(android.provider.CallLog.Calls.TYPE);
        int dateColumn=c.getColumnIndex(android.provider.CallLog.Calls.DATE);

        while(c.moveToNext()){
            CallLogItem item=new CallLogItem();
            item.logType = CallLogItem.LOGTYPE_CALL;
            item.number=c.getString(numberColumn);
            item.dateMil = c.getLong(dateColumn);

            item.callType=c.getInt(typeColumn);
            item.duration=c.getInt(duraColumn);
            Log.i("tag", "duration " + item.duration);

            item.name=c.getString(nameColumn);
            callLogList.add(item);

        }
        c.close();
        return callLogList;
    }

    // deprecated
    public ArrayList<CallLogItem> queryCallLog(int type){
        Cursor c = resolver.query(
                android.provider.CallLog.Calls.CONTENT_URI, null,
                android.provider.CallLog.Calls.TYPE + "='"+type+"'",   null,
                android.provider.CallLog.Calls.DATE + " DESC");
        return getCallLogItemListFromCursor(c);
    }
}