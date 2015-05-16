package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.service.LSPService;

/**
 * Created by summer on 15. 5. 16.
 */
public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("ACTION.RESTART.PersistentService")) {
            LSPLog.onTextMsgForce("RESTART SERVICE BY PERSISTENT ALARM");
            Intent i = new Intent(context, LSPService.class);
            context.startService(i);
        }
    }
}
