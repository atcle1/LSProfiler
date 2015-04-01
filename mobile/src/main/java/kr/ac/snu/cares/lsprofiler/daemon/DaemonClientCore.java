package kr.ac.snu.cares.lsprofiler.daemon;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by summer on 3/28/15.
 */
public class DaemonClientCore {
    public static final String TAG = DaemonClientCore.class.getSimpleName();
    public enum State{ERROR, CONNECTED, DISCONNECTED};
    private State state = State.DISCONNECTED;

    private Context context;
    private Socket clntSock;
    private OutputStream os;
    private DataOutputStream out;
    private DaemonClientReader reader;

    public DaemonClientCore(Context context) {
        this.context = context;
    }

    public void sendInt(int val) {
        if (state != State.CONNECTED)
            return;

        try {
            out.writeInt(val);
            out.flush();
            Log.v(TAG, "write " + val);
        } catch (IOException e) {
            e.printStackTrace();
            setDisconnected();
        }
    }

    public void setDisconnected() {
        // DO NOT call reader thread realated function!
        state = State.DISCONNECTED;
        try{
            clntSock.close();
            clntSock = null;
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public int connect(int port) {
        try {
            if (state == State.CONNECTED) {
                Log.i(TAG, "connect() called, but already connected!");
                return -1;
            }

            if (reader != null) {
                Log.i(TAG, "connected() but reader is not null");
                reader.stopRequest();
                reader.join(1000);
            }

            clntSock = new Socket("127.0.0.1", port);
            os = clntSock.getOutputStream();
            out = new DataOutputStream(os);

            reader = new DaemonClientReader(this, clntSock, clntSock.getInputStream());
            reader.start();

            state = State.CONNECTED;

            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            state = State.ERROR;
            try {
                setDisconnected();
                if (reader != null && reader.state == 1) {
                    reader.stopRequest();
                    reader.join(1000);
                }
                reader = null;
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }

            ex.printStackTrace();
            Log.e(TAG, " " + ex.getMessage());
            Toast.makeText(context, "Err "+ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return -1;
        }
        return 0;
    }


    /*
    public  byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(value >> 24);
        byteArray[1] = (byte)(value >> 16);
        byteArray[2] = (byte)(value >> 8);
        byteArray[3] = (byte)(value);
        return byteArray;
    }

    public  int byteArrayToInt(byte bytes[]) {
        return ((((int)bytes[0] & 0xff) << 24) |
                (((int)bytes[1] & 0xff) << 16) |
                (((int)bytes[2] & 0xff) << 8) |
                (((int)bytes[3] & 0xff)));
    }
    */
}
