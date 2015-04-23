package kr.ac.snu.cares.lsprofiler.daemon;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by summer on 3/27/15.
 */
public class DaemonClient extends Handler {
    public static final String TAG = DaemonClient.class.getSimpleName();
    public static final int DAEMON_CONNECT = 0;
    public static final int DAEMON_SEND    = 1;
    public static final int DAEMON_COLLECT = 2;

    private Context context;
    private DaemonClientCore core;

    public DaemonClient(Looper looper) {
        super(looper);
    }

    public int init(Context context) {
        this.context = context;
        core = new DaemonClientCore(context);
        return 0;
    }

    public void sendMsg(int msg) {
        this.sendEmptyMessage(msg);
    }

    public boolean requestCollectLog() {
        boolean bSuccess = false;
        Log.i(TAG, "requestCollectLog()");
        try {
            core.sendInt(DAEMON_COLLECT);
            core.setWaitForReply(true);
            bSuccess = core.waitForReply();
        }catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return bSuccess;
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case DAEMON_CONNECT:
                core.connect(10001);
                break;
            case DAEMON_SEND:
                core.sendInt(DAEMON_SEND);
                break;
            case DAEMON_COLLECT:
                core.sendInt(DAEMON_COLLECT);
                break;
            default:
                Log.e(TAG, "unknown mesg " + msg.what);
                break;
        }
    }
}
