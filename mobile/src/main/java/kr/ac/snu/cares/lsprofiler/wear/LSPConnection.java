package kr.ac.snu.cares.lsprofiler.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.service.LSPBootService;
import kr.ac.snu.cares.lsprofiler.util.FileLogWritter;

/**
 * Created by summer on 15. 5. 6.
 */
public class LSPConnection implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    public static final String TAG = LSPConnection.class.getSimpleName();
    private Context context;
    private GoogleApiClient mGoogleApiClient; // 구글 플레이 서비스 API 객체
    public int pingpong  = 0;
    Object ping = new Object();

    public LSPConnection(Context context) {
        this.context = context;
        // 구글 플레이 서비스 객체를 시계 설정으로 초기화
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
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

    private boolean isWatch(String name) {
        if (name.contains("G Watch R") || name.contains("Urbane")) {
            return true;
        }
        return false;
    }

    public GoogleApiClient getmGoogleApiClient()
    {
        return mGoogleApiClient;
    }

    public static boolean wearLspConnected = false;
    public boolean isWearConnected() {
        connect();
        wearLspConnected = false;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<Node> connectedNodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
                for (Node node : connectedNodes) {
                    Log.i(TAG, "isWearConnected() " + node.getId() + " " + node.getDisplayName());
                    if (isWatch(node.getDisplayName())) {
                        wearLspConnected = true;
                        break;
                    }

                }
            }
        };
        try {
            Thread t = new Thread(runnable);
            t.start();
            t.join(3000);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return wearLspConnected;
    }

    public static boolean pingResult = false;
    synchronized public boolean sendPing(final int timeoutMills) {
        int sleepMill = 0;
        try {
            pingResult = false;
            Runnable runable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "ping - getConnectedNodes()");
                        connect();
                        NodeApi.GetConnectedNodesResult connectedResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await(timeoutMills, TimeUnit.MILLISECONDS);
                        if (connectedResult == null) {
                            Log.i(TAG, "getConnectedNode return null");
                            pingResult = false;
                            return;
                        }

                        java.util.List<com.google.android.gms.wearable.Node> pingList = connectedResult.getNodes();
                        Node watchNode = null;
                        for (Node node : pingList) {
                            if (isWatch(node.getDisplayName())) {
                                watchNode = node;
                                break;
                            }
                        }
                        if (watchNode == null) {
                            Log.e(TAG, "watchNode is null");
                            pingResult = false;
                            return;
                        } else {
                            Log.e(TAG, "watch node found");
                        }
                        Log.i(TAG, "ping - sendMessage()");
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient,
                                watchNode.getId(), path, "PING".getBytes()).await(timeoutMills, TimeUnit.MILLISECONDS);
                        if (result.getStatus().isSuccess()) {
                            Log.i(TAG, "send ping success");
                            pingResult = true;
                        }
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            Thread t = new Thread(runable);
            t.start();
            t.join(timeoutMills);

        } catch (Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }

        return pingResult;
    }

    public static boolean blockingResult = false;
    class BlockingMessageWorker extends Thread {
        public boolean sendResult;
        public String message;
        public String path;
        public int timeoutMills;
        public long start, end;
        public BlockingMessageWorker(String path, String message, int timeoutMills)
        {
            this.path = path;
            this.message = message;
            this.timeoutMills = timeoutMills;
            sendResult = false;
        }
        @Override
        public void run() {
            try{
                start = System.currentTimeMillis();
                Log.i(TAG, "blocking message - getConnectedNodes()");
                connect();
                NodeApi.GetConnectedNodesResult connectedResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await(timeoutMills, TimeUnit.MILLISECONDS);
                if (connectedResult == null) {
                    Log.i(TAG, "blocking message - getConnectedNode return null");
                    sendResult = false;
                    return;
                }
                end = System.currentTimeMillis();
                Log.i(TAG, "GetConnectedNodes() finished in " + (end-start) +" ms");
                start=end;

                java.util.List<com.google.android.gms.wearable.Node> pingList = connectedResult.getNodes();
                Node watchNode = null;
                for (Node node : pingList) {
                    if (isWatch(node.getDisplayName())) {
                        watchNode = node;
                        break;
                    }
                }
                if (watchNode == null) {
                    FileLogWritter.writeString("blocking message " + message + " / watchNode is null");
                    sendResult = false;
                    return;
                } else {
                    Log.i(TAG, "watch node found");
                }
                Log.i(TAG, "ping - send blocking message()");
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient,
                        watchNode.getId(), path, message.getBytes()).await(timeoutMills, TimeUnit.MILLISECONDS);
                if (result.getStatus().isSuccess()) {
                    end = System.currentTimeMillis();
                    Log.i(TAG, "sendMessage success finished in " + (end-start) +" ms");
                    sendResult = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                LSPLog.onException(ex);
            }
        }
    }

    synchronized public boolean sendBlockingMessage(String path, String message, final int timeoutMills) {
        int sleepMill = 0;
        BlockingMessageWorker worker = new BlockingMessageWorker(path, message, timeoutMills);
        try {
            worker.start();
            worker.join(timeoutMills);
        } catch (Exception ex) {
            ex.printStackTrace();
            LSPLog.onException(ex);
        }
        return worker.sendResult;
    }


    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    // 구글 플레이 서비스에 접속 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle) {
        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
        Wearable.MessageApi.addListener(mGoogleApiClient, myMessageApiListener);
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
            for (final Node node : nodeList) {
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
            Log.i(TAG, resultString);
        }
    };
    /////

    /*
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);
        Log.i(TAG, "MsgReceived : " + path + " / " + msg + " by "+messageEvent.getSourceNodeId());

        if (messageEvent.getPath().equals("/LSP/WINFO/MAC")) {
            // 텍스트뷰에 적용 될 문자열을 지정한다.

            //Toast.makeText(context, "watch mac " + msg, Toast.LENGTH_SHORT).show();

        } else if (path.equals("/LSP/WINFO/STATUS")) {

            //Toast.makeText(context, "watch status : " + msg, Toast.LENGTH_SHORT).show();
        }else if (path.equals("/LSP/WPONG")) {
                pingpong = 3;
        } else {

            //Toast.makeText(context, "unknown msg " + path + " / " +msg, Toast.LENGTH_SHORT).show();
        }
    }
    */
    MyMessageApiListener myMessageApiListener = new MyMessageApiListener();
    class MyMessageApiListener implements  MessageApi.MessageListener {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            String path = messageEvent.getPath();
            final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);
            Log.i(TAG, "MsgReceived : " + path + " / " + msg + " by "+messageEvent.getSourceNodeId());

            if (messageEvent.getPath().equals("/LSP/WINFO/MAC")) {
                // 텍스트뷰에 적용 될 문자열을 지정한다.

                //Toast.makeText(context, "watch mac " + msg, Toast.LENGTH_SHORT).show();

            } else if (path.equals("/LSP/WINFO/STATUS")) {

                //Toast.makeText(context, "watch status : " + msg, Toast.LENGTH_SHORT).show();
            }else if (path.equals("/LSP/WPONG")) {
                pingpong = 3;
            } else {

                //Toast.makeText(context, "unknown msg " + path + " / " +msg, Toast.LENGTH_SHORT).show();
            }
        }
    }
}