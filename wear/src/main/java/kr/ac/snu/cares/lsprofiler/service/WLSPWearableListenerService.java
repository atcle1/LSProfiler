package kr.ac.snu.cares.lsprofiler.service;

import android.widget.Toast;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import kr.ac.snu.cares.lsprofiler.LSPApplication;

/**
 * Created by summer on 15. 5. 6.
 */
public class WLSPWearableListenerService extends WearableListenerService {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 페어링이 되면 실행된다.
    @Override // NodeApi.NodeListener
    public void onPeerConnected(Node node) {
        Toast.makeText(getApplication(), "Peer Connected", Toast.LENGTH_SHORT).show();
    }

    // 페어링이 해제되면 실행된다.
    @Override // NodeApi.NodeListener
    public void onPeerDisconnected(Node node) {
        Toast.makeText(getApplication(), "Peer Disconnected", Toast.LENGTH_SHORT).show();
    }

    // 메시지가 수신되면 실행되는 메소드
    @Override // MessageApi.MessageListener
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/MESSAGE_PATH")) {
            // 텍스트뷰에 적용 될 문자열을 지정한다.
            final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);

            if (msg.equals("START")) {
                LSPApplication.getInstance().startProfiling();
            } else if (msg.equals("STOP")) {
                LSPApplication.getInstance().stopProfiling();
            } else if (msg.equals("REPORT")) {
                LSPApplication.getInstance().doWearReport();
            }

            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
