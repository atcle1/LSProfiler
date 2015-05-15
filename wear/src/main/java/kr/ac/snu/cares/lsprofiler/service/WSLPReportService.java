package kr.ac.snu.cares.lsprofiler.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.LSPReporter;
import kr.ac.snu.cares.lsprofiler.util.ReportItem;

/**
 * Created by summer on 15. 5. 8.
 */
public class WSLPReportService extends Service {
    public static final String TAG = WSLPReportService.class.getSimpleName();
    private LSPApplication lspApplication;
    private LSPReporter reporter;
    private int status = 0;
    private String serverMac = "";
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btClientSocket = null;
    private BluetoothDevice btDevice;
    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;
    private static WLSPHandler wlspHandler;
    private static final int REQUEST_DOREPORT = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        reporter = new LSPReporter(LSPApplication.getInstance());
        lspApplication = LSPApplication.getInstance();
        pm = (PowerManager)getApplication().getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WSLPReportService");

        HandlerThread thread = new HandlerThread("WSLPreportService Thread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper looper = thread.getLooper();
        wlspHandler = new WLSPHandler(looper);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    boolean resumeWhenFinished;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //reporter.requestReportToDaemon();
        Log.i(TAG, "onStarttCommand " + intent.toString() + " " + flags + " " + startId + " " + status);
        if (intent != null) {
            serverMac = intent.getStringExtra("serverMac");
            resumeWhenFinished = intent.getBooleanExtra("resumeWhenFinished", true);

            if (serverMac == null) {
                Log.e(TAG, "server mac is null!!!!!!!!!!1");
                if (resumeWhenFinished)
                    lspApplication.resumeLogging();

            }

            Message msg = wlspHandler.obtainMessage(REQUEST_DOREPORT);
            if (resumeWhenFinished)
                msg.arg1 = 1;
            else
                msg.arg1 = 0;

            if (!wlspHandler.hasMessages(REQUEST_DOREPORT)) {
                Log.i(TAG, "send message to wlspHandler");
                wlspHandler.sendMessage(msg);
            }
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    byte[] fileBuffer = new byte[1024 * 4];
    private boolean sendFileWithId(int id, File file) {
        int len;
        Log.i(TAG, "sendFileWithId()" + id + " " + file);
        try {

            DataOutputStream dos = new DataOutputStream(btClientSocket.getOutputStream());
            if (id == 0) {
                dos.writeInt(0);
                return true;
            }

            FileInputStream fis = new FileInputStream(file);
            if (fis == null) {
                Log.i(TAG, file.getAbsolutePath() + "new FileInputStream() return null");
                return false;
            }
            // write id (1 byte)
            dos.writeInt(id);

            // write filename
            dos.writeUTF(file.getName());

            // send file size (8 byte)
            dos.writeLong(file.length());
            dos.flush();
            Log.i(TAG, "file send start " + file.toString() + " size " + file.length());
            // send file data
            BufferedInputStream bis = new BufferedInputStream(fis, 1024 * 32);
            int prevLen = 0;
            while( (len = bis.read(fileBuffer, 0, fileBuffer.length)) != -1) {
                //btOutputStream.write(fileBuffer, 0, len);
                if (prevLen != len) {
                    Log.i(TAG, "write "+len);
                    prevLen = len;
                }
                dos.write(fileBuffer, 0, len);
                dos.flush();
            }
            Log.i(TAG, "file send end " + file.getName() );

            bis.close();
            fis.close();

            dos.flush();

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
        boolean bBackup = true;

        reporter.collectReport(item);
        if (item.fileList == null || item.fileList.size() == 0) {
            Log.i(TAG, "collectReport - list is null or size == 0");
            return false;
        }
        try {
            boolean bBTSetupSuccess = setupBtClient();
            if (bBTSetupSuccess) {
                byte cnt = 1;
                Log.i(TAG, "doReport() send total " + item.fileList.size());
                for (File file : item.fileList) {
                    Log.i(TAG, "file : " + file.getName());
                }
                for (File file : item.fileList) {
                    boolean bSendFileSuccess = sendFileWithId(cnt, file);
                    if (bSendFileSuccess) {
                        cnt++;
                    } else {
                        Log.i(TAG, "send file failed " + file);
                        break;
                    }
                }
                Log.i(TAG, "send all file complete, send 0");
                sendFileWithId(0, null);
                Log.i(TAG, "send files " + ((int) cnt - 1));
            } else {
                Log.i(TAG, "setupBtClient failed!");
                bBackup = false;        // next time, retry
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (bBackup)
                reporter.backupReports(item);
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
        }catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private class WLSPHandler extends Handler  {
        public WLSPHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    break;
                case REQUEST_DOREPORT:
                    try {
                        wakeLock.acquire(1000 * 300);
                        if (status == 0 && serverMac != null && !serverMac.equals("")) {
                            status = 1;
                            doReport();
                            if (msg.arg1 == 1)
                                lspApplication.resumeLogging();
                            status = 0;
                        }
                    }catch (Exception ex) {
                        LSPLog.onTextMsgForce("ERR WSLREPorterService "+ex.getMessage());
                    } finally {
                        if (wakeLock != null)
                            wakeLock.release();
                    }
                    stopSelf();

                    //application.doReport();
                    break;
                default :
                    break;
            }
        }
    }
}
