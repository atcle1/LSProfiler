package kr.ac.snu.cares.lsprofiler.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.MainActivity;
import kr.ac.snu.cares.lsprofiler.R;

public class LSPService extends Service {
    public static final String ACTION_ALARM = "kr.ac.snu.cares.lsprofiler.LSPService.ALARM";
    public static final String TAG = LSPService.class.getSimpleName();
    public static final int ALARM_REQUEST = 123121;
    public LSPHandler lspHandler = new LSPHandler();
    private LSPApplication application;

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
        application = (LSPApplication)getApplication();
    }
    private static final int NOTIFICATION_ID = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() %d %d" + flags + " " + startId);
        Toast.makeText(this, TAG + " onStartCommand", Toast.LENGTH_SHORT).show();
        if (startId == 0) {

        }
        if (intent == null) {
            // start by system
            Log.i(TAG, "intent == null");
        } else {
            int requestCode = intent.getExtras().getInt("requestCode");
            if (requestCode == LSPService.ALARM_REQUEST) {
                // backup
                application.doReport();
            }
        }
        //showForegroundNotification("Running LSProfiler in foreground");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, TAG + " onDestroy", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDestroy()");
    }

    private class LSPHandler extends Handler  {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    break;
                case ALARM_REQUEST:
                    application.doReport();
                    break;
                default :
                    break;
            }
        }
    }

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
}