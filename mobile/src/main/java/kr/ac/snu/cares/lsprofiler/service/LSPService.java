package kr.ac.snu.cares.lsprofiler.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.MainActivity;
import kr.ac.snu.cares.lsprofiler.R;

public class LSPService extends Service {
    public static final String ACTION_ALARM = "kr.ac.snu.cares.lsprofiler.LSPService.ALARM";
    public static final String TAG = LSPService.class.getSimpleName();
    public static final int ALARM_REQUEST = 123121;
    private static LSPHandler lspHandler;
    private LSPApplication application;

    public LSPService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        //Toast.makeText(this, TAG + " onBind", Toast.LENGTH_SHORT).show();
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        //Toast.makeText(this, TAG + " onCreate()", Toast.LENGTH_SHORT).show();
        application = (LSPApplication)getApplication();

        HandlerThread thread = new HandlerThread("LSPService handler", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper looper = thread.getLooper();
        lspHandler = new LSPHandler(looper);

    }
    private static final int NOTIFICATION_ID = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() : flags " + flags + " startId " + startId + " " + intent);
        Toast.makeText(this, TAG + " onStartCommand " + startId, Toast.LENGTH_SHORT).show();
        LSPLog.onTextMsg("LSPService onStart()");


        if (intent == null) {
            // service is terminated by system, and restarted. start by system
            Log.i(TAG, "onStartCommand() : intent == null");
            LSPLog.onTextMsgForce("start by system");
        } else {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return START_STICKY;
            }
            int requestCode = bundle.getInt("requestCode");
            if (requestCode == LSPService.ALARM_REQUEST) {
                // first start routine...
                // backup
                //application.doReport();
                lspHandler.sendEmptyMessage(ALARM_REQUEST);
            }
        }


        //showForegroundNotification("Running LSProfiler in foreground");
        Log.i(TAG, "onStartCommand return");
        return START_STICKY;
    }

    public static Handler getHandler() {
        return lspHandler;
    }
    @Override
    public void onDestroy() {
        lspHandler = null;
        try {
            Toast.makeText(getApplicationContext(), TAG + " onDestroy", Toast.LENGTH_SHORT).show();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.i(TAG, "onDestroy()");
        LSPLog.onTextMsg("LSPService onDestroy()");
    }

    private class LSPHandler extends Handler  {
        public LSPHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "LSPHandler "+msg);
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


    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LSPService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}