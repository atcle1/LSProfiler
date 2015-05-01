package kr.ac.snu.cares.lsprofiler;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import kr.ac.snu.cares.lsprofiler.daemon.DaemonClient;
import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.email.Mail;
import kr.ac.snu.cares.lsprofiler.resolvers.CallLogItem;
import kr.ac.snu.cares.lsprofiler.resolvers.CallLogResolver;
import kr.ac.snu.cares.lsprofiler.resolvers.SmsLogResolver;
import kr.ac.snu.cares.lsprofiler.util.CallLogMerger;
import kr.ac.snu.cares.lsprofiler.util.Su;
import kr.ac.snu.cares.lsprofiler.util.Util;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private Button btStartStresser;
    private Button btStopStresser;

    private Button btConnect;
    private Button btSend;

    private Button btInsertLog;
    private Button btReadLog;

    private Button btSendMail;
    private Button btCallLog;


    private DaemonClient clientHandler;
    HandlerThread daemonClientThread;
    LSPApplication lspApplication;


    private void setButton(int id, View.OnClickListener listener)
    {
        Button button = (Button)findViewById(id);
        button.setOnClickListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onServiceBtClickListener serviceBtClickListener = new onServiceBtClickListener();

        btStartStresser = (Button)findViewById(R.id.bTStartService);
        btStopStresser = (Button)findViewById(R.id.bTStopService);
        btConnect = (Button)findViewById(R.id.bTConnect);
        btSend = (Button)findViewById(R.id.bTSend);
        btInsertLog = (Button)findViewById(R.id.bTInsertLog);
        btReadLog = (Button)findViewById(R.id.bTReadLog);
        btSendMail = (Button)findViewById(R.id.bTSendMail);
        btCallLog = (Button)findViewById(R.id.bTCallLog);
        lspApplication = (LSPApplication)getApplication();

        btStartStresser.setOnClickListener(serviceBtClickListener);
        btStopStresser.setOnClickListener(serviceBtClickListener);
        btConnect.setOnClickListener(serviceBtClickListener);
        btSend.setOnClickListener(serviceBtClickListener);
        btInsertLog.setOnClickListener(serviceBtClickListener);
        btReadLog.setOnClickListener(serviceBtClickListener);
        btSendMail.setOnClickListener(serviceBtClickListener);
        btCallLog.setOnClickListener(serviceBtClickListener);

        setButton(R.id.bTBackupLog, serviceBtClickListener);

        /*
        daemonClientThread = new HandlerThread("daemon client thread");
        daemonClientThread.start();
        clientHandler = new DaemonClient(daemonClientThread.getLooper());
        clientHandler.init(this);
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class onServiceBtClickListener implements View.OnClickListener
    {
        Su su;
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.bTStartService) {
                // start daemon
                Toast.makeText(v.getContext(), "start", Toast.LENGTH_SHORT).show();
                //DaemonStarter.startDaemon();
                //lspApplication.startLogging();
                Su.isRooted();
                lspApplication.startProfiling();

            } else if (v.getId() == R.id.bTStopService) {
                Toast.makeText(v.getContext(), "stop", Toast.LENGTH_SHORT).show();

                //lspApplication.stopLogging();
                lspApplication.stopProfiling();



            } else if (v.getId() == R.id.bTConnect) {
                //clientHandler.sendMsg(DaemonClient.DAEMON_CONNECT);
                lspApplication.doKLogBackup();


            } else if (v.getId() == R.id.bTSend) {
                //clientHandler.sendMsg(DaemonClient.DAEMON_SEND);
                    Log.i(TAG, "is rooted ? " + Su.isRooted());
//                Log.i(TAG, "su available () : " + Shell.SU.available());

            } else if (v.getId() == R.id.bTInsertLog) {
                //LogDbHandler logDbHandler = ((LSPApplication) getApplication()).getDbHandler();
                //logDbHandler.writeLog("test "+ Math.random());
                su = new Su();
                su.prepare();
                su.execSu("id");
                su.stopSu(100);
            } else if (v.getId() == R.id.bTReadLog) {
                LogDbHandler logDbHandler = ((LSPApplication) getApplication()).getDbHandler();
                logDbHandler.printLog();
                Log.i(TAG, "onClick() - btReadLog()");
            } else if (v.getId() == R.id.bTSendMail) {
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Log.i(TAG, "mail send start");
                        Mail.sendTest();
                        Log.i(TAG, "mail send end");
                    }
                });
            } else if (v.getId() == R.id.bTCallLog) {
                CallLogResolver callLogResolver = new CallLogResolver(getContentResolver());
                SmsLogResolver smsLogResolver = new SmsLogResolver(getContentResolver());

                ArrayList<CallLogItem> smsList = smsLogResolver.getSmsLog(Util.getTodayTimestamp());
                ArrayList<CallLogItem> callList = callLogResolver.queryCallLog(Util.getTodayTimestamp());
                ArrayList<CallLogItem> mergeList = CallLogMerger.merge(callList, smsList);

                Log.i(TAG, "callList total : "+ callList.size());
                for (CallLogItem i : callList) {
                    Log.i(TAG, i.summary());
                }
                Log.i(TAG, "smsList total : "+ smsList.size());
                for (CallLogItem i : smsList) {
                    Log.i(TAG, i.summary());
                }
                Log.i(TAG, "mergeList total : "+ mergeList.size());
                for (CallLogItem i : mergeList) {
                    Log.i(TAG, i.summary());
                }

            } else if (v.getId() == R.id.bTBackupLog) {
                lspApplication.doReport();
            }
        }
    }
}
