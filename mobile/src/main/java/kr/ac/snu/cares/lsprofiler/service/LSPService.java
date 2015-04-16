package kr.ac.snu.cares.lsprofiler.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import kr.ac.snu.cares.lsprofiler.MainActivity;
import kr.ac.snu.cares.lsprofiler.R;
import kr.ac.snu.cares.lsprofiler.receivers.ReceiverManager;

public class LSPService extends Service {
    public static final String TAG = LSPService.class.getSimpleName();
    ReceiverManager receiverManager;

    public LSPService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        Toast.makeText(this, TAG + " onBind", Toast.LENGTH_SHORT).show();
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        Toast.makeText(this, TAG + " onCreate()", Toast.LENGTH_SHORT).show();
    }
    private static final int NOTIFICATION_ID = 1;

    private void showForegroundNotification(String contentText) {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        //NotificationManager mgr=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.common_signin_btn_icon_light)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        startForeground(NOTIFICATION_ID, notification);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        Toast.makeText(this, TAG + " onStartCommand", Toast.LENGTH_SHORT).show();
        if (intent == null) {
            // start by system
        } else {

        }
        //showForegroundNotification("Running LSProfiler in foreground");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, TAG + " onDestroy", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDestroy()");
    }
}