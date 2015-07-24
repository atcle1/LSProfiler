package kr.ac.snu.cares.lsprofiler.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 3/28/15.
 */
public class LSPNotificationService extends NotificationListenerService{
    public static final String TAG = LSPNotificationService.class.getSimpleName();
    private LPNotificationServiceReceiver lpNotificationServiceReceiver;

    public static void startSelf(Context context) {
        // start service
        Intent startServiceIntent = new Intent(context, LSPNotificationService.class);
        //startServiceIntent.putExtra("setting", setting);

        context.startService(startServiceIntent);
    }

    public static void stopSelf(Context context) {
        context.stopService(new Intent(context, LSPNotificationService.class));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(lpNotificationServiceReceiver);
    }

    class LPNotificationServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive() - " + intent.getAction());
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //Log.i(TAG, "onNotificationPosted()");
        //LSPLog.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //Log.i(TAG, "onNotificationRemoved()");
        //LSPLog.onNotificationRemoved(sbn);
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        //Log.i(TAG, "onNotificationRankingUpdate()");

    }
}