package kr.ac.snu.cares.lsprofiler;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtStatus;
    private TextView mTextView;
    private onBtClickListener btClickListener;
    private LSPApplication lspApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btClickListener = new onBtClickListener();
        lspApplication = (LSPApplication)getApplication();


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                txtStatus = (TextView) findViewById(R.id.txtStatus);
                setButton(R.id.bTStatus, btClickListener);
                updateStatus();
            }
        });
    }

    private void setButton(int id, View.OnClickListener listener)
    {
        Button button = (Button)findViewById(id);
        if (button != null)
            button.setOnClickListener(listener);
        else {
            Log.e(TAG, "button not found");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    public void updateStatus() {
        String status = "";
        status += "status : " + lspApplication.state + "\n";
        if (txtStatus != null)
            txtStatus.setText(status);
    }

    class onBtClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.bTStatus) {
                updateStatus();
            }
        }
    }
}
