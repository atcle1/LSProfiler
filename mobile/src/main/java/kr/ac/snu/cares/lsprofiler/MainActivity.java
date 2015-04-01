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

import kr.ac.snu.cares.lsprofiler.daemon.DaemonClient;
import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.email.Mail;
import kr.ac.snu.cares.lsprofiler.service.LSPService;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private Button btStartStresser;
    private Button btStopStresser;

    private Button btConnect;
    private Button btSend;

    private Button btInsertLog;
    private Button btReadLog;

    private Button btSendMail;


    private DaemonClient clientHandler;
    HandlerThread daemonClientThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btStartStresser = (Button)findViewById(R.id.bTStartService);
        btStopStresser = (Button)findViewById(R.id.bTStopService);

        btConnect = (Button)findViewById(R.id.bTConnect);
        btSend = (Button)findViewById(R.id.bTSend);

        btInsertLog = (Button)findViewById(R.id.bTInsertLog);
        btReadLog = (Button)findViewById(R.id.bTReadLog);

        btSendMail = (Button)findViewById(R.id.bTSendMail);

        onServiceBtClickListener serviceBtClickListener = new onServiceBtClickListener();
        btStartStresser.setOnClickListener(serviceBtClickListener);
        btStopStresser.setOnClickListener(serviceBtClickListener);
        btConnect.setOnClickListener(serviceBtClickListener);
        btSend.setOnClickListener(serviceBtClickListener);
        btInsertLog.setOnClickListener(serviceBtClickListener);
        btReadLog.setOnClickListener(serviceBtClickListener);

        btSendMail.setOnClickListener(serviceBtClickListener);

        daemonClientThread = new HandlerThread("daemon client thread");
        daemonClientThread.start();
        clientHandler = new DaemonClient(daemonClientThread.getLooper());
        clientHandler.init(this);
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
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.bTStartService) {
                Toast.makeText(v.getContext(), "start", Toast.LENGTH_SHORT).show();

                // start service
                Intent startServiceIntent = new Intent(v.getContext(), LSPService.class);
                //startServiceIntent.putExtra("setting", setting);
                startServiceIntent.putExtra("first_start", true);
                startService(startServiceIntent);
            } else if (v.getId() == R.id.bTStopService) {
                Toast.makeText(v.getContext(), "stop", Toast.LENGTH_SHORT).show();
                stopService(new Intent(v.getContext(), LSPService.class));
            } else if (v.getId() == R.id.bTConnect) {
                clientHandler.sendMsg(DaemonClient.DAEMON_CONNECT);

            } else if (v.getId() == R.id.bTSend) {
                clientHandler.sendMsg(DaemonClient.DAEMON_SEND);
            } else if (v.getId() == R.id.bTInsertLog) {
                LogDbHandler logDbHandler = ((LSPApplication) getApplication()).getDbHandler();
                logDbHandler.writeLog("test "+ Math.random());
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
            }
        }
    }
}
