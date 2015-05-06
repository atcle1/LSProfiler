package kr.ac.snu.cares.lsprofiler.wear;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by summer on 15. 5. 6.
 */
public class LSPConnection implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
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
    }

    public void conneect() {
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

    private void sendMessage(Node node) {
        byte[] bytes;
        if (message != null) {
            bytes = message.getBytes();
        } else {
            return;
        }

        // 메시지 전송 및 전송 후 실행 될 콜백 함수 지정
        Wearable.MessageApi.sendMessage(mGoogleApiClient,
                node.getId(), "/MESSAGE_PATH", bytes)
                .setResultCallback(resultCallback);
    }

    // Send Message 버튼을 클릭했을 때 실행
    public void sendMessage(String message) {
        // 전송할 메시지 텍스트 생성
        this.message = message;

        // 페어링 기기들을 지칭하는 노드를 가져온다.
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

                    // 노드를 가져온 후 실행된다.
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult
                                                 getConnectedNodesResult) {

                        // 노드를 순회하며 메시지를 전송한다.
                        for (final Node node : getConnectedNodesResult.getNodes()) {
                            sendMessage(node);
                        }
                    }
                });
    }

    // 시계로 데이터 및 메시지를 전송 후 실행되는 메소드
    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(Result result) {

            String resultString = "Sending Result : " + result.getStatus().isSuccess();

            Toast.makeText(context, resultString, Toast.LENGTH_SHORT).show();
        }
    };
}
