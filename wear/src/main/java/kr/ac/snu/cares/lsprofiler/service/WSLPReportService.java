package kr.ac.snu.cares.lsprofiler.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPReporter;

/**
 * Created by summer on 15. 5. 8.
 */
public class WSLPReportService extends Service {
    Handler handler;
    LSPApplication lspApplicationl;
    LSPReporter reporter;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        reporter = new LSPReporter(LSPApplication.getInstance());
        lspApplicationl = LSPApplication.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //reporter.requestReportToDaemon();

        


        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lspApplicationl = LSPApplication.getInstance();
        lspApplicationl.startProfilingIfStarted();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


            if (msg.what == 0) {

            }
        }
    }


}
