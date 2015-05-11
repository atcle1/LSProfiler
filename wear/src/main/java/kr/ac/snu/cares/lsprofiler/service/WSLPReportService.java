package kr.ac.snu.cares.lsprofiler.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPReporter;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;

/**
 * Created by summer on 15. 5. 8.
 */
public class WSLPReportService extends Service {
    public static final String TAG = WSLPReportService.class.getSimpleName();
    private Handler myHandler;
    private LSPApplication lspApplication;
    private LSPReporter reporter;
    private int status = 0;
    private String serverMac = "";
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btClientSocket = null;
    private BluetoothDevice btDevice;
    private OutputStream btOutputStream;
    private PowerManager pm;


    @Override
    public void onCreate() {
        super.onCreate();
        myHandler = new MyHandler();
        reporter = new LSPReporter(LSPApplication.getInstance());
        lspApplication = LSPApplication.getInstance();
        PowerManager pm = (PowerManager) getSystemService( Context.POWER_SERVICE );

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    boolean resumeWhenFinished;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //reporter.requestReportToDaemon();
        pm =  (PowerManager) getSystemService( Context.POWER_SERVICE );
        PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "WSLPReportService" );
        wakeLock.acquire(1000 * 300);
        Log.i(TAG, "onStarttCommand " + intent.toString() + " " + flags + " " + startId + " " + status);
        if (intent != null) {
            serverMac = intent.getStringExtra("serverMac");
            resumeWhenFinished = intent.getBooleanExtra("resumeWhenFinished", true);
            if (serverMac == null) {
                Log.e(TAG, "server mac is null!!!!!!!!!!1");
                if (resumeWhenFinished)
                    lspApplication.resumeLogging();
            }
            if (status == 0) {
                status = 1;
                doReport();
                status = 0;
                if (resumeWhenFinished)
                    lspApplication.resumeLogging();
            }
        }
        wakeLock.release();
        stopSelf();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    byte[] fileBuffer = new byte[1024 * 4];
    private boolean sendFileWithId(byte id, File file) {
        int len;
        try {
            if (id == 0) {
                btOutputStream.write(id);
                return true;
            }

            FileInputStream fis = new FileInputStream(file);
            if (fis == null) {
                Log.i(TAG, file.getAbsolutePath() + "new FileInputStream() return null");
                return false;
            }
            // write id (1 byte)
            btOutputStream.write(id);


            // send fileName byte size
            String fileName = file.getName();
            byte[] fileNameBytes = fileName.getBytes();
            btOutputStream.write((byte) fileNameBytes.length);

            // send fileName
            btOutputStream.write(fileNameBytes);

            // send file size (8 byte)
            byte[] filesize_buffer = ByteBuffer.allocate(8).putLong(file.length()).array();
            btOutputStream.write(filesize_buffer);

            // send file data
            BufferedInputStream bis = new BufferedInputStream(fis, 1024 * 4);
            while( (len = bis.read(fileBuffer)) != -1) {
                Log.i("read file", "size: "+len);
                btOutputStream.write(fileBuffer, 0, len);
                Log.i("write bt", "end");
            }
            Log.i(TAG, "file send end " +file.getName() );

            bis.close();
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean doReport() {
        Log.i(TAG, "doReport() start");
        ReportItem item = new ReportItem();

        reporter.collectReport(item);
        if (item.fileList == null || item.fileList.size() == 0) {
            Log.i(TAG, "collectReport - list is null or size == 0");
            return false;
        }

        boolean bBTSetupSuccess = setupBtClient();
        if (bBTSetupSuccess) {
            //item.fileList
            //btOutputStream.write();
            byte cnt = 1;
            for (File file : item.fileList) {
                boolean bSendFileSuccess = sendFileWithId(cnt, file);
                if (bSendFileSuccess) {
                    cnt++;
                } else {
                    Log.i(TAG, "send file failed " + file);
                    break;
                }
            }
            sendFileWithId((byte)0, null);
            Log.i(TAG, "send files "+ ((int)cnt-1));
        } else {
            Log.i(TAG, "setupBtClient failed!");
            return false;
        }
        Log.i(TAG, "doReport() end");
        return true;
    }

    private boolean setupBtClient() {
        try {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            btDevice = btAdapter.getRemoteDevice(serverMac);
            btClientSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            btClientSocket.connect();
            btOutputStream = btClientSocket.getOutputStream();
        }catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {

            }
        }
    }


}
