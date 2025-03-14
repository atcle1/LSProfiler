package kr.ac.snu.cares.lsprofiler.pref;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by summer on 3/28/15.
 */
public class LSPPreferenceManager {
    private Context context;
    private SharedPreferences prefs;
    private static LSPPreferenceManager lprPrefMnager;

    private LSPPreferenceManager(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        lprPrefMnager = this;
    }

    public static LSPPreferenceManager getInstance(Context context) {
        return new LSPPreferenceManager(context);
    }

    /**
     * Could be return null.
     * @return instance or null
     */
    public static LSPPreferenceManager getInstance() {
        return lprPrefMnager;
    }

    public void setLoggingState(String state) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("logging_state", state);
        editor.commit();
        Log.i("LSP", "set logging state "+state);
    }

    public String getLoggingState() {
        String state = prefs.getString("logging_state", "unknown");
        return state;
    }

    public void setAppState(String state) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_state", state);
        editor.commit();
        Log.i("LSP", "set app state "+state);
    }

    public String getAppState() {
        String state = prefs.getString("app_state", "unknown");
        return state;
    }

    public void setDeviceID(String deviceID) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("deviceID", deviceID);
        editor.commit();
    }

    public String getDeviceID() {
        String deviceID = prefs.getString("deviceID", "");
        return deviceID;
    }

    public void setWearEnabled(Boolean bEnabled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("wear_enabled", bEnabled);
        editor.commit();
    }

    public Boolean getWearEnabled() {
        return prefs.getBoolean("wear_enabled", true);
    }

    public void setAlarmEnabled(boolean bEnabled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("alarm_enabled", bEnabled);
        editor.commit();
    }
    public boolean getAlarmEnabled() {
        return prefs.getBoolean("alarm_enabled", false);
    }

    public void setAlarmTime(int hour, int min) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("alarm_hour", hour);
        editor.putInt("alarm_min", min);
        editor.commit();
    }

    public int[] getAlarmTime() {
        int hour_min[] = new int[2];
        int hour = prefs.getInt("alarm_hour", 0);
        int min = prefs.getInt("alarm_min", 0);

        hour_min[0] = hour;
        hour_min[1] = min;

        return hour_min;
    }
}
