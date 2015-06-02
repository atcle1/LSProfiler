package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 3/28/15.
 */
public class ReceiverManager extends BroadcastReceiver {
    public static final String TAG = ReceiverManager.class.getSimpleName();
    private Context context;
    private MyPhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private boolean isRegisteredReceivers = false;

    public ReceiverManager(Context context) {
        this.context = context;
        phoneStateListener = new MyPhoneStateListener();
    }

    public void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        //filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        filter.addAction("kr.ac.snu.lsprofiler.intent.action.TOPACTIVITY_RESUMEING");

        context.registerReceiver(this, filter);

        if (phoneStateListener == null) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    phoneStateListener = new MyPhoneStateListener();
                    telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                }
            });

        } else {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        isRegisteredReceivers = true;
        Log.i(TAG, "registerReceivers()");
    }

    public void unregisterReceivers() {
        try {
            context.unregisterReceiver(this);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            isRegisteredReceivers = false;
        }catch (Exception ex) {
            //ex.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_SCREEN_ON)){
            LSPLog.onScreenChagned(1);
        }  else if(action.equals(Intent.ACTION_SCREEN_OFF)) {
            LSPLog.onScreenChagned(0);
        } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            // battery stat
            LSPLog.onBatteryStatusChagned(intent);
        } else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            // sms received
                LSPLog.onSmsReceived(intent);
        } else if (action.equals("kr.ac.snu.lsprofiler.intent.action.TOPACTIVITY_RESUMEING")) {
            // activity resuming
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String packageName = (String) bundle.getString("packageName");
                LSPLog.onTopActivityResuming(packageName);
                Log.i(TAG, "top activity : " + packageName);
            }
        } else {
            Log.i(TAG, action);
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            LSPLog.onCallStateChanged(state, incomingNumber);
        }
    }
}