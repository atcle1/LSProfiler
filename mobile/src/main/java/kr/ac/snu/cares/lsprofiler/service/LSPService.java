package kr.ac.snu.cares.lsprofiler.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        Toast.makeText(this, TAG + " onStartCommand", Toast.LENGTH_SHORT).show();
        if (intent == null) {
            // start by system
        } else {

        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, TAG + " onDestroy", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDestroy()");
    }
}