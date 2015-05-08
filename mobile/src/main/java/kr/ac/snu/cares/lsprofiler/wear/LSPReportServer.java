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

/**
 * Created by summer on 15. 5. 8.
 */
public class LSPReportServer extends Thread {
    BluetoothAdapter mBluetoothAdapter = null;

    //private int buffersize = 4;
    private int buffersize = 1024 * 4;
    private byte[] buffer = new byte[buffersize];
    private byte[] filesizeBuffer = new byte[8];
    private long FileSize;
    //private byte[] buffer;
    private String filename = "/sdcard/33Mfile";


    private String data;
    //int last_byte_count;
    int ReadBytes;
    long BytesReceived;
    int mLen;

    public LSPReportServer() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void receiveFile(InputStream in, String path) {
//get the filesizeBuffer
        try {
            FileOutputStream fout = new FileOutputStream(path);

            in.read(filesizeBuffer, 0, 8);
            ByteBuffer BB = ByteBuffer.wrap(filesizeBuffer);
            FileSize = BB.getLong();

            System.out.println("****************" + "Received FileSize" + FileSize + "****************");

            BytesReceived = 0;
            while (BytesReceived < FileSize) {
                mLen = in.read(buffer);
                if (mLen > 0) {
                    BytesReceived += mLen;
                    Log.i("read from bt", "size: " + mLen);
                    fout.write(buffer, 0, mLen);
                    Log.i("writeFile", "end");
                } else {
                    System.out.println("Received -1, EOF");
                    break;
                }
            }
            System.out.println("****************" + "EOF" + "****************");

            in.close();
            fout.close();
        }catch (Exception ex) {

        }
    }

    //int i=32;
    public void run() {
        System.out.println("****************" + "Server Start" + "****************");
        BluetoothServerSocket serverSocket;
        BluetoothSocket socket = null;

        try {
            serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("JustService", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket = serverSocket.accept();

            InputStream in = socket.getInputStream();

            receiveFile(in, "/sdcard/test");


            serverSocket.close();
            socket.close();

        } catch (Exception e) {
            System.out.println("EXCEPTION::" + e.getMessage());
        }

    }
}
