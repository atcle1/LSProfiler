package kr.ac.snu.cares.lsprofiler.receivers;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import kr.ac.snu.cares.lsprofiler.LSPLog;

/**
 * Created by summer on 4/15/15.
 */
public class LocationTracer implements LocationListener {
    public static final String TAG = LocationTracer.class.getSimpleName();
    private AlarmManager alarmManager;
    private static final long GPS_ALARM_INTERVAL = 1000 * 60 * 60;  // 1 hour
    //private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000;  // 10 m
    //private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 60; // 1 hour
    private Context context;
    private LocationManager locationManager;
    private Criteria criteria = new Criteria();
    // 현재 GPS 사용유무
    private boolean isGPSEnabled = false;
    // 네트워크 사용유무
    private boolean isNetworkEnabled = false;

    private TimeoutableLocationListener timeoutableLocationListener;

    private PendingIntent pendingIntent;

    public LocationTracer(Context context) {
        this.context = context;
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setSpeedRequired(false);
        criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    public void startTrace() {
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "getSystemService(location_service) return null");
        }
        checkState();
        if (!isGPSEnabled && !isNetworkEnabled) {
            showSettingsAlert();
        }

        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        context.registerReceiver(gpsAlarmReceiver, new IntentFilter(GPSAlarmIntentStr));

                Intent intent = new Intent(GPSAlarmIntentStr);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,  AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),1000 * 20, pendingIntent);
    }

    public void requestUpdate() {
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            provider = LocationManager.GPS_PROVIDER;
        }

        locationManager.requestLocationUpdates(provider, 0, 0, this);
        timeoutableLocationListener = new TimeoutableLocationListener(locationManager, 1000 * 5, this);
        Log.i(TAG, "requestUpdate()");
    }

    public void checkState() {
        // GPS 정보 가져오기
        isGPSEnabled = locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER);

        // 현재 네트워크 상태 값 알아오기
        isNetworkEnabled = locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER);
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다."+
                "\n 설정창으로 가시겠습니까?");
                // OK 를 누르게 되면 설정창으로 이동합니다.
                alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void stopTrace() {
        locationManager.removeUpdates(this);
        if (pendingIntent != null)
            alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LSPLog.onLocationUpdate(location.getProvider(), latitude, longitude);
        Log.i(TAG, "onLocationUpdate() "+location.getProvider()+" "+latitude + " "  + longitude);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private BroadcastReceiver gpsAlarmReceiver = new GPSAlarmReceiver();
    public static final String GPSAlarmIntentStr = GPSAlarmReceiver.class.getName()+".ALARM";
    private class GPSAlarmReceiver extends android.content.BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "GPS AlarmReceiver onReceive()");
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location2 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LSPLog.onKnownLocation("NET", latitude, longitude);
            } else if (location2 != null) {
                double latitude = location2.getLatitude();
                double longitude = location2.getLongitude();
                LSPLog.onKnownLocation("GPS", latitude, longitude);
            }

            requestUpdate();
        }
    }


}
