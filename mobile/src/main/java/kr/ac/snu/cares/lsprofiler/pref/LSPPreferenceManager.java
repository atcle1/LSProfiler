package kr.ac.snu.cares.lsprofiler.pref;

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
}
