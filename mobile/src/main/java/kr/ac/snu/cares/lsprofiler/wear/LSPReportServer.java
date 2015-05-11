package kr.ac.snu.cares.lsprofiler.wear;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import kr.ac.snu.cares.lsprofiler.LSPReporter;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;

/**
 * Created by summer on 15. 5. 8.
 */
public class LSPReportServer extends Thread {
    public static final String TAG = LSPReportServer.class.getSimpleName();
    BluetoothAdapter mBluetoothAdapter = null;

    //private int buffersize = 4;
    private int buffersize = 1024 * 4;
    private byte[] buffer = new byte[buffersize];
    private byte[] filesizeBuffer = new byte[8];

    private long FileSize;

    //int last_byte_count;
    long bytesReceived;
    int mLen;

    public LSPReportServer() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int receiveFileWithId(InputStream in, String dirPath) {
        //get the filesizeBuffer
        int id = -1;
        try {
            // receive file name length
            id = in.read();
            if (id == 0)
                return 0;

            int fileNameLen = in.read();
            if (fileNameLen <= 0) {
                Log.i(TAG, "receiveFileName len <= 0 " + fileNameLen);
                return -1;
            }

            // receive file name
            bytesReceived = 0;
            while(bytesReceived < fileNameLen) {
                int readCnt = in.read(buffer, (int)bytesReceived, (int)(fileNameLen - bytesReceived));
                if (readCnt > 0)
                    bytesReceived += readCnt;
                else {
                    // error
                    Log.i(TAG, "receiveFiles name error read return "  + readCnt);
                    return -1;
                }
            }
            String fileName = new String(buffer, 0, fileNameLen);
            Log.i(TAG, "receive file name : " + fileName);

            // receive file size
            in.read(filesizeBuffer, 0, 8);
            ByteBuffer BB = ByteBuffer.wrap(filesizeBuffer);
            FileSize = BB.getLong();

            // receive file data
            FileOutputStream fileOutputStream = new FileOutputStream(dirPath + "/" + fileName);
            Log.i(TAG, "****************" + "Received FileSize" + FileSize + "****************");
            bytesReceived = 0;
            while (bytesReceived < FileSize) {
                mLen = in.read(buffer);
                if (mLen > 0) {
                    bytesReceived += mLen;
                    Log.i("read from bt", "size: " + mLen);
                    fileOutputStream.write(buffer, 0, mLen);
                    Log.i("writeFile", "end");
                } else {
                    System.out.println("Received -1, EOF");
                    break;
                }
            }

            fileOutputStream.close();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return id;
    }

    public void receiveFiles(InputStream in, String path) {
        try {
            int id, receivedCnt = 0;
            while (true) {
                id = receiveFileWithId(in, path);
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
            File f = new File(LSPReporter.COLLECT_PATH);
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

            receiveFiles(in, "/sdcard/WLSP");

            serverSocket.close();
            socket.close();
        } catch (Exception e) {
            Log.i(TAG, "EXCEPTION::" + e.getMessage());
        }
        Log.i(TAG, "server end");
    }
}
