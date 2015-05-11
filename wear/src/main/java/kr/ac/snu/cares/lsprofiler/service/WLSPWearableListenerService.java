package kr.ac.snu.cares.lsprofiler.service;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.security.spec.MGF1ParameterSpec;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.util.Util;

/**
 * Created by summer on 15. 5. 6.
 */
public class WLSPWearableListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    public static final String TAG = WLSPWearableListenerService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체
    private Node node;
    private String nodeId;
    private Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        context = getApplicationContext();
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    // 페어링이 되면 실행된다.
    @Override // NodeApi.NodeListener
    public void onPeerConnected(Node node) {
        Toast.makeText(getApplication(), "Peer Connected", Toast.LENGTH_SHORT).show();
        this.node = node;
        nodeId = node.getId();
    }

    // 페어링이 해제되면 실행된다.
    @Override // NodeApi.NodeListener
    public void onPeerDisconnected(Node node) {
        Toast.makeText(getApplication(), "Peer Disconnected", Toast.LENGTH_SHORT).show();
        this.node = null;
    }

    // 메시지가 수신되면 실행되는 메소드
    @Override // MessageApi.MessageListener
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);

        if (node == null)
            mGoogleApiClient.connect();

        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "MsgReceived : " + path + " / " + msg + " by "+messageEvent.getSourceNodeId());

        if (path.equals("/LSP/CONTROL")) {
            // 텍스트뷰에 적용 될 문자열을 지정한다.
            if (msg.equals("START")) {
                LSPApplication.getInstance().startProfiling();
            } else if (msg.equals("STOP")) {
                LSPApplication.getInstance().stopProfiling();
            } else if (msg.startsWith("REPORT")) {
                String[] splits = msg.split(" ");
                String mac = null;
                if (splits.length == 2) {
                    mac = splits[1];
                    LSPApplication.getInstance().doWearReport(mac);
                } else
                    return;
            }
        } else if (path.equals("/LSP/WINFO")) {
            if (msg.equals("STATUS")) {
                String state = LSPApplication.getInstance().state.name();
                Log.i(TAG,"send state "+state);
                Wearable.MessageApi.sendMessage(mGoogleApiClient,
                        messageEvent.getSourceNodeId(), "/LSP/WINFO/STATUS", state.getBytes())
                        .setResultCallback(resultCallback);
            } else if (msg.equals("MAC")) {
                String mac = Util.getBluetoothAddress();
                Log.i(TAG,"send mac "+mac);
                if (mac == null) return;
                Wearable.MessageApi.sendMessage(mGoogleApiClient,
                        messageEvent.getSourceNodeId(), "/LSP/WINFO/MAC", mac.getBytes())
                        .setResultCallback(resultCallback);
            }

        }

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // 시계로 데이터 및 메시지를 전송 후 실행되는 메소드
    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(Result result) {

            String resultString = "Sending Result : " + result.getStatus().isSuccess();

            Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Sending Result : " + result.getStatus().isSuccess());
        }
    };
}
