package kr.ac.snu.cares.lsprofiler.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by summer on 4/24/15.
 */
public class Su {
    private Runtime runtime;
    private Process su;
    DataOutputStream outputStream;
    BufferedReader reader;
    InputStream inputStream;
    private ReaderThread readerThread;

    public Su() {
        runtime = Runtime.getRuntime();
    }

    public int prepare() {
        try {
            su = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(su.getOutputStream());
            inputStream = su.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            readerThread = new ReaderThread(null, reader);
            readerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int stopSu() {
        Log.i("Su", "StopSu()");
        int exitCode = -1;

        try {
            execSu("exit");
            exitCode = su.waitFor();
            readerThread.bRun = false;
            readerThread.interrupt();
            for (int i = 0; i < 5; i++) {
                if (readerThread.isAlive()) {
                    Log.i("stopSu", "alive()! " + i);
                    if (i>2)
                        readerThread.interrupt();
                    readerThread.join(1000);
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return exitCode;
    }

    public void execSu(String comm) {
        try{
            outputStream.writeBytes(comm + "\n");
            outputStream.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    class ReaderThread extends Thread {
        private Handler handler;
        private BufferedReader reader;
        private StringBuffer buffer;
        public Boolean bRun = false;
        public ReaderThread(Handler handler, BufferedReader reader) {
            this.handler = handler;
            this.reader = reader;
            buffer = new StringBuffer();
        }

        public void run() {
            String line;
            bRun = true;
            while (bRun) {
                try {
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                        System.out.println(line);
                        reply(line);
                    }
                } catch (IOException io) {
                    System.out.println("brun "+ bRun);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.i("ReaderThread", "run()end");
        }

        private void reply(String line) {
            if (handler == null) return;
            Message msg = handler.obtainMessage();
            msg.what = 0;
            msg.obj = line;
            handler.sendMessage(msg);
        }
    }

}
