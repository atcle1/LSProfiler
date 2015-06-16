package kr.ac.snu.cares.lsprofiler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import kr.ac.snu.cares.lsprofiler.daemon.DaemonClient;
import kr.ac.snu.cares.lsprofiler.db.LogDbHandler;
import kr.ac.snu.cares.lsprofiler.email.Mail;
import kr.ac.snu.cares.lsprofiler.resolvers.DumpsysResolver;
import kr.ac.snu.cares.lsprofiler.resolvers.FitnessResolver;
import kr.ac.snu.cares.lsprofiler.service.LSPService;
import kr.ac.snu.cares.lsprofiler.util.Su;
import kr.ac.snu.cares.lsprofiler.wear.LSPConnection;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtStatus;

    private Button btStartStresser;
    private Button btStopStresser;
    private Button btStatus;
    private Button btReadLog;
    private Button btSendMail;
    private Button btCallLog;

    private DaemonClient clientHandler;
    private HandlerThread daemonClientThread;
    private LSPApplication lspApplication;

    private LSPConnection connection;

    private void setButton(int id, View.OnClickListener listener)
    {
        Button button = (Button)findViewById(id);
        button.setOnClickListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onBtClickListener btClickListener = new onBtClickListener();

        txtStatus = (TextView)findViewById(R.id.txtStatus);

        btStartStresser = (Button)findViewById(R.id.bTStartService);
        btStopStresser = (Button)findViewById(R.id.bTStopService);
        btReadLog = (Button)findViewById(R.id.bTReadLog);
        btSendMail = (Button)findViewById(R.id.bTSendMail);
        btCallLog = (Button)findViewById(R.id.bTCallLog);
        lspApplication = (LSPApplication)getApplication();

        btStartStresser.setOnClickListener(btClickListener);
        btStopStresser.setOnClickListener(btClickListener);
        btReadLog.setOnClickListener(btClickListener);
        btSendMail.setOnClickListener(btClickListener);
        btCallLog.setOnClickListener(btClickListener);

        setButton(R.id.bTBackupLog, btClickListener);
        setButton(R.id.bTStatus, btClickListener);
        setButton(R.id.bTRoot, btClickListener);

        /*
        daemonClientThread = new HandlerThread("daemon client thread");
        daemonClientThread.start();
        clientHandler = new DaemonClient(daemonClientThread.getLooper());
        clientHandler.init(this);
        */
        connection = new LSPConnection(getApplicationContext());

        //buildFitnessClient();
        Log.i(TAG, "onCreate() end");
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
    private void updateStatus() {
        String status = "";
        status += "status : " + lspApplication.state + "\n";
        status += "nextAlarm : " + LSPAlarmManager.getNextAlarm().getTime().toString() + "\n";
        status += "watch : " + lspApplication.getLSPConnection().isWearConnected()+ "\n";
        txtStatus.setText(status);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        updateStatus();
        lspApplication.getFitnessResolver().setMainActivity(this);
        Log.i(TAG, "onResume() end");
    }

    @Override
    protected void onPause() {
        super.onPause();
        lspApplication.getFitnessResolver().setMainActivity(null);
    }
    static boolean pop = false;
    class onBtClickListener implements View.OnClickListener
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
                lspApplication.getFitnessResolver().connect();
                updateStatus();
            } else if (v.getId() == R.id.bTStopService) {
                Toast.makeText(v.getContext(), "stop", Toast.LENGTH_SHORT).show();

                //lspApplication.stopLogging();
                lspApplication.stopProfiling();
                updateStatus();
            } else if (v.getId() == R.id.bTReadLog) {
                LogDbHandler logDbHandler = ((LSPApplication) getApplication()).getDbHandler();
                logDbHandler.printLog();
                logDbHandler.printLog2();
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
                /*
                if (connection.sendPing(10000)) {
                    Toast.makeText(getApplicationContext(), "PONG", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No PONG!!!", Toast.LENGTH_SHORT).show();
                }
                */
                /*
                DumpsysResolver dumpsysResolver = new DumpsysResolver();
                dumpsysResolver.doWriteDumpAsync("");
                dumpsysResolver.joinDumpAsync(1000 * 10);
                */
                // dumpsy
/*
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        lspApplication.getReporter().collectFitnessReport(1000 * 30);
                    }
                });
*/

                Log.i(TAG, "onClick btBackupLog end");

                if (pop) {
                    notificationRemove();
                    pop = false;
                } else {
                    notificationPop();
                    pop = true;
                }
            } else if (v.getId() == R.id.bTStatus) {
                updateStatus();
            } else if (v.getId() == R.id.bTRoot) {
                boolean isRooted = Su.isRooted();
                Toast.makeText(getApplicationContext(), "Rooted : "+isRooted, Toast.LENGTH_SHORT).show();
            } else if (v.getId() == R.id.bTBackupLog) {
                ReportThread reportThread = new ReportThread();
                reportThread.start();


            }
        }
    }

    private class ReportThread extends Thread {
        @Override
        public void run() {
            lspApplication.doReport();
        }
    }

    private void notificationPop() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setTicker("Notification.Builder");
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setSmallIcon(R.drawable.common_signin_btn_icon_dark);
        mBuilder.setNumber(10);
        mBuilder.setContentTitle("Notification.Builder Title");
        mBuilder.setContentText("Notification.Builder Massage");
        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        nm.notify(111, mBuilder.build());
    }

    private void notificationRemove() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(111);
    }


}
