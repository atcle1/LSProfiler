package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

    public ReceiverManager() {
        Log.e(TAG, "receiver manager ()");

    }

    public ReceiverManager(Context context) {
        this.context = context;
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
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");

        context.registerReceiver(this, filter);

        phoneStateListener = new MyPhoneStateListener();
        telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
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
            Log.i(TAG, "Screen ON");
            LSPLog.onScreenChagned(1);
        }  else if(action.equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(TAG, "Screen OFF");
            LSPLog.onScreenChagned(0);
        } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            Log.i(TAG, action);
            LSPLog.onBatteryStatusChagned(intent);
        } else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            Object messages[] = (Object[])bundle.get("pdus");
            SmsMessage smsMessage[] = new SmsMessage[messages.length];

            for(int i = 0; i < messages.length; i++) {
                // PDU 포맷으로 되어 있는 메시지를 복원합니다.
                smsMessage[i] = SmsMessage.createFromPdu((byte[])messages[i]);
            }
            // SMS 수신 시간 확인
            Date curDate = new Date(smsMessage[0].getTimestampMillis());
            Log.d("문자 수신 시간", curDate.toString());
            // SMS 발신 번호 확인
            String origNumber = smsMessage[0].getOriginatingAddress();
            // SMS 메시지 확인
            String message = smsMessage[0].getMessageBody().toString();
            Log.d("문자 내용", "발신자 : "+origNumber+", 내용 : " + message);
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