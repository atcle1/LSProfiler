package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.LSPReporter;
import kr.ac.snu.cares.lsprofiler.resolvers.DumpsysResolver;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;

/**
 * Created by summer on 3/26/15.
 */
public class ShutdownReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {
        int i = 0;
        Log.v("ShutdownReceiver", "action - " + intent.getAction());

        LSPLog.onTextMsg("Shutdown");

        // power off
        LSPLog.onPowerStateChagned(0);
        LSPApplication app = LSPApplication.getInstance();
        if (app != null && (app.state == LSPApplication.State.resumed || app.state == LSPApplication.State.paused))
            app.doKLogBackup();

        // dumpsys start
        ReportItem item = new ReportItem();
        DumpsysResolver dumpsysResolver = new DumpsysResolver();
        dumpsysResolver.doWriteDumpAsync(LSPReporter.COLLECT_MOBILE_PATH + item.reportDateString + ".dump.txt");
    }
}
