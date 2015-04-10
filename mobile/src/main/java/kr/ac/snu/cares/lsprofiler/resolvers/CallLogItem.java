package kr.ac.snu.cares.lsprofiler.resolvers;

import android.provider.CallLog;

import java.util.Date;

/**
 * Created by summer on 4/9/15.
 */
public class CallLogItem {
    public CallLogItem(){}
    final public static int LOGTYPE_CALL = 1;
    final public static int LOGTYPE_SMS = 2;

    // common
    public long dateMil = 0;
    public String number = null;
    public int logType = 0;

    // call
    public int duration = 0;
    public int callType = 0;

    // sms
    public String message = null;
    public int smsType = 0;

    // deprecated
    public String name = null;

    public String getName(){
        if(name!=null) return name;
        return getFormattedNumber();
    }
    public String getFormattedNumber(){
        if(number.length()==9){
            // 02-XXX-XXXX
            return number.substring(0, 2)+"-"+number.substring(2, 5)+"-"+number.substring(5, 9);
        }else if(number.length()==10){
            //XXX-XXX-XXXX
            //XX-XXXX-XXXX
            if(number.substring(0, 2).equals("02")){
                //02-XXXX-XXXX
                return number.substring(0, 2)+"-"+number.substring(2, 6)+"-"+number.substring(6, 10);
            }else
                return number.substring(0, 3)+"-"+number.substring(3, 6)+"-"+number.substring(6, 10);
        }else if(number.length()==11){
            //XXX-XXXX-XXXX
            return number.substring(0, 3)+"-"+number.substring(3, 7)+"-"+number.substring(7, 11);
        }
        return number;
    }

    public String getDurationString(){
        if(duration<60) return duration+" sec";
        return String.format("%d min  %02d sec", duration/60, duration%60);
    }

    public String summary() {
        String strLogType = "";
        String strSubType = "";
        if (logType == 1) {
            strLogType = "call";
            switch (callType) {
                case CallLog.Calls.INCOMING_TYPE:
                    strSubType = "ic";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    strSubType = "ms";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    strSubType = "og";
                    break;
                default:
                    break;
            }
        } else if (logType == 2) {
            strLogType = "sms";
            switch (smsType) {
                case 1:
                    strSubType = "rc";
                    break;
                case 2:
                    strSubType = "sd";
                    break;
            }
        }
        Date d = new Date(dateMil);

        if (logType == 1) {
            return  d + " " +dateMil + " " + strLogType + " " + strSubType + " " + duration;
        } else if (logType == 2){
            return d + " " +dateMil+ " " + strLogType + " " + strSubType + " " + message.replaceAll("\n","");
        } else {
            return "unknown callLogItem";
        }

    }
}
