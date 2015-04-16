package kr.ac.snu.cares.lsprofiler.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.daemon.DaemonClientCore;

/**
 * Created by summer on 3/28/15.
 */
public class LSPNotificationService extends NotificationListenerService{
    public static final String TAG = DaemonClientCore.class.getSimpleName();
    private LPNotificationServiceReceiver lpNotificationServiceReceiver;

    public void startSelf(Context context) {
        // start service
        Intent startServiceIntent = new Intent(this, LSPNotificationService.class);
        //startServiceIntent.putExtra("setting", setting);
        context.startService(startServiceIntent);
    }

    public void stopSelf(Context context) {
        stopService(new Intent(context, LSPNotificationService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        lpNotificationServiceReceiver = new LPNotificationServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(".service.LPNotificationService");
        registerReceiver(lpNotificationServiceReceiver,filter);

        Log.i(TAG, "onCreate()");
    }

    class LPNotificationServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive() - " + intent.getAction());
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.i(TAG, "onNotificationPosted()");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.i(TAG, "onNotificationRemoved()");
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);
        Log.i(TAG, "onNotificationRankingUpdate()");
    }
}