package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;

import java.util.Date;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 3/28/15.
 */
public class ReceiverManager extends BroadcastReceiver {
    public static final String TAG = ReceiverManager.class.getSimpleName();
    private Context context;
    private boolean isRegisteredReceivers = false;

    public ReceiverManager(Context context) {
        this.context = context;
    }

    public void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        //filter.addAction(Intent.ACTION_BATTERY_LOW);
        //filter.addAction(Intent.ACTION_BATTERY_OKAY);
        //filter.addAction(Intent.ACTION_POWER_CONNECTED);
        //filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        //filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        //filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        //filter.addAction("kr.ac.snu.lsprofiler.intent.action.TOPACTIVITY_RESUMEING");

        context.registerReceiver(this, filter);

        isRegisteredReceivers = true;
        Log.i(TAG, "registerReceivers()");
    }

    public void unregisterReceivers() {
        try {
            context.unregisterReceiver(this);
            isRegisteredReceivers = false;
        }catch (Exception ex) {
            //ex.printStackTrace();
        }
    }
    public void test(){
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        boolean screenOn = false;
        for (Display display : dm.getDisplays()) {
            Log.i(TAG, "display state "  +display.getName() + " " + display.getState() + " " + display.getDisplayId());
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            Log.i(TAG, "Screen ON");
            LSPLog.onScreenChagned(1);
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(TAG, "Screen OFF");
            LSPLog.onScreenChagned(0);
        } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            LSPLog.onBatteryStatusChagned(intent);
        } else {
            Log.i(TAG, action + " : " + intent);
        }
    }
}