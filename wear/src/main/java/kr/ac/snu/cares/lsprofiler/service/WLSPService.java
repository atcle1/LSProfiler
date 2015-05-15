package kr.ac.snu.cares.lsprofiler.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by summer on 4/26/15.
 */
public class WLSPService extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
