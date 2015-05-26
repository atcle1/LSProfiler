package kr.ac.snu.cares.lsprofiler.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import kr.ac.snu.cares.lsprofiler.LSPReporter;
import kr.ac.snu.cares.lsprofiler.util.Su;

/**
 * Created by summer on 4/29/15.
 */
public class LSPBootService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        Su.isRooted();
        Su su = new Su();

        su.prepare();
        su.execSu("su 0 setenforce 0");
        //su.execSu("/data/local/sprofiler 3 "+ LSPReporter.COLLECT_PATH +" "+ "test.klog");
        su.execSu("dumpsys batterystats --enable full-wake-history");
        su.execSu("dumpsys batterystats --disable no-auto-reset");
        su.stopSu(5000);

        this.stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.stopSelf();
        return Service.START_NOT_STICKY;
    }
}
