package kr.ac.snu.cares.lsprofiler.wear;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.UUID;

import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.LSPReporter;

/**
 * Created by summer on 15. 5. 8.
 */
public class LSPReportServer extends Thread {
    public static final String TAG = LSPReportServer.class.getSimpleName();
    BluetoothAdapter mBluetoothAdapter = null;

    //private int buffersize = 4;
    private final int buffersize = 1024 * 8;
    private byte[] buffer = new byte[buffersize];
    private byte[] filesizeBuffer = new byte[8];

    private long FileSize;
    private boolean bCompleted = false;
    public boolean isCompleted() {
        return bCompleted;
    }

    //int last_byte_count;
    long bytesReceived;
    int mLen;

    public LSPReportServer() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int receiveFileWithId(DataInputStream din, String dirPath) {
        //get the filesizeBuffer
        int id = -1;

        try {
            // receive file name length
            id = din.readInt();
            if (id == 0)
                return 0;
            Log.i(TAG, "receive file id : "+id);

            String fileName = din.readUTF();
            Log.i(TAG, "receive file name : " + fileName);

            FileSize = din.readLong();

            File f = new File(dirPath + "/" + fileName);
            if (f.exists()) {
                f = new File(dirPath + "/" + fileName + ".2");
                if (f.exists())
                    f.delete();
            }

            // receive file data
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            BufferedOutputStream bfos = new BufferedOutputStream(fileOutputStream, 1024 * 48);
            //Log.i(TAG, "****************" + "Received FileSize " + FileSize + "****************");
            Calendar startCal = Calendar.getInstance();
            bytesReceived = 0;
            int prev_mLen = 0;
            while (bytesReceived < FileSize) {
                //mLen = in.read(buffer);
                mLen = din.read(buffer, 0, buffer.length < (int)(FileSize - bytesReceived) ?
                                            buffer.length : (int)(FileSize - bytesReceived));
                if (mLen > 0) {
                    bytesReceived += mLen;
                    if (prev_mLen != mLen) {
                        //Log.i(TAG, "din.read " + mLen);
                        prev_mLen = mLen;
                    }
                    //Log.i(TAG, "total received " + mLen + " / " + bytesReceived + " / " + FileSize);
                    bfos.write(buffer, 0, mLen);
                    //fileOutputStream.write(buffer, 0, mLen);
                } else {
                    //System.out.println("Received -1, EOF");
                    break;
                }
            }
            bfos.close();
            fileOutputStream.close();
            Calendar endCal = Calendar.getInstance();
            Long elasped = endCal.getTimeInMillis() - startCal.getTimeInMillis() + 1;
            Log.i(TAG, "elasped time "+( elasped/ 1000.0) +" " + FileSize/1024.0/(elasped/1000.0) + " KB/s");
            Log.i(TAG, "receive file end name : " + fileName);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return id;
    }

    public void receiveFiles(DataInputStream din, String path) {
        try {
            int id, receivedCnt = 0;
            while (true) {
                id = receiveFileWithId(din, path);
                if (id > 0) {
                    Log.i(TAG, "file received " + id);
                    receivedCnt++;
                } else if (id == 0) {
                    Log.i(TAG, "received file total : " + receivedCnt);
                    break;
                } else if (id < 0) {
                    Log.i(TAG, "received err : " + id);
                    return;
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //int i=32;
    public void run() {
        Log.i(TAG, "****************" + "Server Start" + "****************");
        BluetoothServerSocket serverSocket;
        BluetoothSocket socket = null;

        try {
            File f = new File(LSPReporter.COLLECT_WEAR_PATH);
            if (!f.exists()) {
                f.mkdirs();
                if (!f.exists()) {
                    Log.i(TAG, "mkdirs failed ");
                    return;
                }
            }

            serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("JustService", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket = serverSocket.accept();
            InputStream in = socket.getInputStream();

            receiveFiles(new DataInputStream(new BufferedInputStream(in)), LSPReporter.COLLECT_WEAR_PATH);

            serverSocket.close();
            socket.close();
        } catch (Exception e) {
            Log.i(TAG, "EXCEPTION::" + e.getMessage());
            e.printStackTrace();
            LSPLog.onException(e);
        }
        Log.i(TAG, "server end");
        bCompleted = true;
    }
}
