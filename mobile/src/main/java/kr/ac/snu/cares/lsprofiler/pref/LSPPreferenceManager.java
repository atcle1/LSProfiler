package kr.ac.snu.cares.lsprofiler.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    public void setServiceState(String state) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("service_state", "state");
        editor.commit();
    }

    public String getServiceState() {
        String state = prefs.getString("service_state", "unknown");
        return state;
    }
}
