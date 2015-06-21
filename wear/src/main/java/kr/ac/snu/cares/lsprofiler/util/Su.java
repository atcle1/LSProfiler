package kr.ac.snu.cares.lsprofiler.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 4/24/15.
 */
public class Su {
    public static final String TAG = Su.class.getSimpleName();
    private Runtime runtime;
    private Process su;

    BufferedReader reader;
    OutputStream stdinStream;
    InputStream stdStream;
    InputStream errStream;
    private StreamGobbler stdoutStreamGobbler;
    private StreamGobbler stderrStreamGobbler;
    private boolean bReady = false;
    private List<String> stdoutList = new ArrayList<String>();
    private List<String> stderrList = new ArrayList<String>();
    public Su() {
        runtime = Runtime.getRuntime();
    }

    private static class Worker extends Thread {
        private final Process process;
        private Integer exit;
        private Worker(Process process) {
            this.process = process;
        }
        public void run() {
            try {
                exit = process.waitFor();
            } catch (InterruptedException ignore) {
                return;
            }
        }
    }

    public static int executeCommandLine(final String commandLine,
                                         final long timeout)
            throws IOException, InterruptedException, TimeoutException
    {
        Runtime runtime = null;
        Process process = null;
        Worker worker = null;
        try {
            runtime = Runtime.getRuntime();
            process = runtime.exec(commandLine);
            worker = new Worker(process);
            worker.start();

            worker.join(timeout);
            if (worker.exit != null)
                return worker.exit;
            else {
                worker.interrupt();
            }
        } catch(Exception ex) {
            FileLogWritter.writeException(ex);
            try {
                if (worker != null)
                    worker.interrupt();
            }catch (Exception ex2) {
                ex.printStackTrace();
                FileLogWritter.writeException(ex2);
            }

        } finally {
            if (process != null)
                process.destroy();
        }
        return -1;
    }

    public static int executeSuOnce(String cmd, int timeout) {
        try {
            executeCommandLine("su -c " + cmd, timeout);
        } catch (Exception e) {
            LSPLog.onException(e);
            e.printStackTrace();
        }
        return 0;
    }

    public int prepare() {
        try {
            su = Runtime.getRuntime().exec("sh -c su");

            stdStream = su.getInputStream();
            stdoutStreamGobbler = new StreamGobbler(stdStream, stdoutList);

            errStream = su.getErrorStream();
            stderrStreamGobbler = new StreamGobbler(errStream, stderrList);

            stdinStream = su.getOutputStream();

            stdoutStreamGobbler.start();
            stderrStreamGobbler.start();
            bReady = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int stopSu(){
        return stopSu(0);
    }

    public int stopSu(int sleep) {
        //Log.i(TAG, "StopSu()");
        int exitCode = -1;
        if (bReady == false)
            return 0;

        try {
            if (sleep != 0)
                Thread.sleep(sleep);
            su.destroy();
            stdoutStreamGobbler.interrupt();
            stderrStreamGobbler.interrupt();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return exitCode;
    }

    public void execSu(String comm) {
        try{
            Log.i(TAG, "#< " + comm);
            stdinStream.write((comm + "\n").getBytes());
            stdinStream.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    /*
        class ReaderThread extends Thread {
            private Handler handler;
            private InputStream inputStream;
            private InputStreamReader inputStreamReader;
            private BufferedReader reader;
            private StringBuffer buffer;
            public Boolean bRun = false;
            public ReaderThread(Handler handler, InputStream inputStream) {
                this.handler = handler;
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);
                buffer = new StringBuffer();
            }

            public void run() {
                String line;
                bRun = true;
                while (bRun) {
                    try {
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line + "\n");
                            //System.out.println(line);
                            Log.i("LSP", "reader.readLine() "+ line);
                            reply(line);
                        }

                        Log.i(TAG, "readLine() next");
                        bRun = false;
                    } catch (IOException io) {
                        System.out.println("brun "+ bRun);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                Log.i(TAG, "run()end");
            }

            private void reply(String line) {
                if (handler == null) return;
                Message msg = handler.obtainMessage();
                msg.what = 0;
                msg.obj = line;
                handler.sendMessage(msg);
            }
        }
        */
    public static boolean isRooted() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("su -c id");
            InputStream inputStream = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bfr = new BufferedReader(isr);
            String output = bfr.readLine();
            if (output.contains("uid=0")) {
                Log.i(TAG, "rooted " + output);
                return true;
            }
            process.destroy();
            Log.i(TAG, "not rooted");
            return false;
        }catch (Exception ex) {
            ex.printStackTrace();
            Log.i(TAG, "not rooted");
            return false;
        }
    }
}