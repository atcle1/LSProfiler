package kr.ac.snu.cares.lsprofiler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPApplication;
import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 15. 5. 26.
 */
public class PackageReceiver extends BroadcastReceiver {
    public static final String TAG = PackageReceiver.class.getSimpleName();
    public PackageReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            Log.d(TAG, "PAD  data = " + intent.getData());
            LSPLog.onPackageAdded(intent.getData().toString());
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            Log.d(TAG, "PRM  data = " + intent.getData());
            LSPLog.onPackageRemoved(intent.getData().toString());
        } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            Log.d(TAG, "PRP  data = " + intent.getData());
            LSPLog.onPackageReplaced(intent.getData().toString());
        }
    }
}
