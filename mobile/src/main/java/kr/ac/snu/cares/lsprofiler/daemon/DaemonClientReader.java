package kr.ac.snu.cares.lsprofiler.daemon;

import android.util.Log;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by summer on 3/28/15.
 */
public class DaemonClientReader extends Thread {
    public static final String TAG = DaemonClientReader.class.getSimpleName();
    public int state;
    private DaemonClientCore core;
    private Socket clntSock;
    private InputStream is;
    private DataInputStream dis;

    public DaemonClientReader(DaemonClientCore core, Socket clntSock, InputStream is)
    {
        this.core = core;
        this.clntSock = clntSock;
        this.is = is;
        dis = new DataInputStream(is);
        state = 0;
    }

    public void stopRequest(){
        if (state == 2)
            return;
        state = 2;
        interrupt();
    }

    @Override
    public void run() {
        state = 1;
        while (clntSock.isClosed() == false && state == 1) {
            try {
                int i = dis.readInt();
                Log.i(TAG, "receiver " + i);


                if (core.bWaittingReplay)
                    core.bReplayed = true;
            } catch (java.io.EOFException ex){
                core.setDisconnected();
                break;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Log.i(TAG, "run() return...");
        state = 2;
    }
}
