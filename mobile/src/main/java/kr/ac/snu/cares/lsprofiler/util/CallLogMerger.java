package kr.ac.snu.cares.lsprofiler.util;

import java.util.ArrayList;

import kr.ac.snu.cares.lsprofiler.resolvers.CallLogItem;

/**
 * Created by summer on 4/10/15.
 */
public class CallLogMerger {
    public static ArrayList<CallLogItem> merge(ArrayList<CallLogItem> callLogList,
                                            ArrayList<CallLogItem> smsLogList)
    {
        // TODO : merge operation is not optimal.
        ArrayList<CallLogItem> result = new ArrayList<CallLogItem>();
        CallLogItem smsItem;
        result.addAll(callLogList);

        for (int i = 0; i < smsLogList.size(); i++)
        {
            smsItem = smsLogList.get(i);
            // first delete the both receive and send sms log in result list.
            for (int k = 0; k < result.size(); k++) {
                if(result.get(k).dateMil == smsItem.dateMil) {
                    result.remove(k);
                    break;
                }
            }

            // add send sms log
            if (smsItem.smsType == 2) {
                result.add(smsItem);
            }
        }
        return result;
    }
}
