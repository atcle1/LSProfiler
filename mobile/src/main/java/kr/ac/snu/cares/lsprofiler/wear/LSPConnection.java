package kr.ac.snu.cares.lsprofiler.wear;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by summer on 15. 5. 6.
 */
public class LSPConnection implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        MessageApi.MessageListener {
    public static final String TAG = LSPConnection.class.getSimpleName();
    private Context context;
    private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체

    public LSPConnection(Context context) {
        this.context = context;
        // 구글 플레이 서비스 객체를 시계 설정으로 초기화
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        connect();
    }

    public void connect() {
        // 구글 플레이 서비스에 접속돼 있지 않다면 접속한다.
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    // 구글 플레이 서비스에 접속 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle) {
        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
    }

    // 구글 플레이 서비스에 접속이 일시정지 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int i) {
        Toast.makeText(context, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    // 구글 플레이 서비스에 접속을 실패했을 때 실행
    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(context, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    String message = "";
    String path = "";

    private void sendMessageToNode(Node node) {
        byte[] bytes;
        if (message != null) {
            bytes = message.getBytes();
        } else {
            return;
        }

        // 메시지 전송 및 전송 후 실행 될 콜백 함수 지정
        Wearable.MessageApi.sendMessage(mGoogleApiClient,
                node.getId(), path, bytes)
                .setResultCallback(resultCallback);
    }

    private void sendMessageToNode(Node node, String path, String message) {
        byte[] bytes;
        if (message != null) {
            bytes = message.getBytes();
        } else {
            return;
        }

        // 메시지 전송 및 전송 후 실행 될 콜백 함수 지정
        Wearable.MessageApi.sendMessage(mGoogleApiClient,
                node.getId(), path, bytes)
                .setResultCallback(resultCallback);
        Log.i(TAG, "sendMessageToNode " + node.getId() + " " + path + " / " + message);
    }

    class GetNodeListCallbackReply implements ResultCallback<NodeApi.GetConnectedNodesResult> {
        private String path;
        private String message;
        public GetNodeListCallbackReply(String path, String message) {
            this.path = path;
            this.message = message;
        }
        @Override
        public void onResult(NodeApi.GetConnectedNodesResult
                                             getConnectedNodesResult) {
            nodeList = getConnectedNodesResult.getNodes();
            for (final Node node : getConnectedNodesResult.getNodes()) {
                Log.i(TAG, "node name " + node.getDisplayName() + " " + node.getId());
                sendMessageToNode(node, path, message);
            }
        }
    }
    java.util.List<com.google.android.gms.wearable.Node> nodeList;
    // Send Message 버튼을 클릭했을 때 실행
    public void sendMessage(String path, String message) {
        connect();
        // 전송할 메시지 텍스트 생성
        //this.message = message;
        //this.path = path;
        nodeList = null;

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new GetNodeListCallbackReply(path, message));

        /*
        if (nodeList != null) {
            for (Node node : nodeList) {
                sendMessageToNode(node, path, message);
            }
        }
        */
    }

    // 시계로 데이터 및 메시지를 전송 후 실행되는 메소드
    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(Result result) {
            String resultString = "Sending Result : " + result.getStatus().isSuccess();
            Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show();
        }
    };
    /////

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);
        Log.i(TAG, "MsgReceived : " + path + " / " + msg + " by "+messageEvent.getSourceNodeId());

        if (messageEvent.getPath().equals("/LSP/WINFO/MAC")) {
            // 텍스트뷰에 적용 될 문자열을 지정한다.

            //Toast.makeText(context, "watch mac " + msg, Toast.LENGTH_SHORT).show();

        } else if (path.equals("LSP/WINFO/STATUS")) {

            //Toast.makeText(context, "watch status : " + msg, Toast.LENGTH_SHORT).show();
        } else {

            //Toast.makeText(context, "unknown msg " + path + " / " +msg, Toast.LENGTH_SHORT).show();
        }
    }
}
