package kr.ac.snu.cares.lsprofiler.receivers;

/**
 * Created by summer on 5/1/15.
 */

import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * TimeoutableLocationListner is implementation of LocationListener.
 * If onLocationChanged isn't called within XX mili seconds, automatically remove listener.
 *
 * @author amay077 - http://twitter.com/amay077
 */
public class TimeoutableLocationListener {
    public static final String TAG = TimeoutableLocationListener.class.getSimpleName();
    protected Timer timerTimeout = new Timer();
    protected LocationManager locaMan = null;
    protected LocationListener locationListener;

    /**
     * Initialize instance.
     *
     * @param locaMan the base of LocationManager, can't set null.
     * @param timeOutMS timeout elapsed (mili seconds)
     * @param locationListener related locationListener
     */
    public TimeoutableLocationListener(LocationManager locaMan, long timeOutMS,
                                       LocationListener locationListener) {
        this.locaMan = locaMan;
        this.locationListener = locationListener;
        timerTimeout.schedule(new TimerTask() {

            @Override
            public void run() {
                stopLocationUpdateAndTimer();
            }
        }, timeOutMS);
    }

    private void stopLocationUpdateAndTimer() {
        locaMan.removeUpdates(locationListener);
        Log.i(TAG, "stopLocationUpdateAndTimer()");
        LSPLog.onTextMsgForce("location listener timeout, remove updates");

        timerTimeout.cancel();
        timerTimeout.purge();
        timerTimeout = null;
    }
}