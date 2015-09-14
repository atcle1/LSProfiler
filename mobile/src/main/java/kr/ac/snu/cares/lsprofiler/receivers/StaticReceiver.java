package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 15. 9. 1.
 */
public class StaticReceiver extends BroadcastReceiver{
    public static final String TAG = StaticReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LSPLog.onTextMsgForce(TAG + " onRecevied action is null");
            return;
        }
        if (action.equals("kr.ac.snu.lsprofiler.intent.action.ACTIVITYSTACK")) {
            // activity resuming
            LSPLog.onFTopActivityResuming(intent);
        } else if (action.equals("kr.ac.snu.lsprofiler.intent.action.NOTIFICATION")) {
            // notification remove
            LSPLog.onNotification(intent);
        } else if (action.equals("kr.ac.snu.lsprofiler.intent.action.STATUSBAR")) {
            // statusbar click
            LSPLog.onFStatusbar(intent);
        } else if (action.equals("kr.ac.snu.lsprofiler.intent.action.PANELBAR")) {
            // panelbar open/close
            LSPLog.onFPanel(intent);
        } else {
            Log.i(TAG, action);
        }
    }
}
