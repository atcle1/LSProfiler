package kr.ac.snu.cares.lsprofiler.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.security.MessageDigest;

/**
 * Created by summer on 4/18/15.
 */
public class DeviceID {
    public static String getDeviceID(Context context) {
        String deviceID;

        deviceID = getPhoneNumber(context);
        if (deviceID == null) {
            deviceID = getMacAddress(context);
        }
        if (deviceID == null) {
            deviceID = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        }
        if (deviceID == null) {
            deviceID = getPseuedoId(context);
        }

        return deviceID;
    }

    static String getPhoneNumber(Context context) {
        TelephonyManager systemService = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = systemService.getLine1Number();
        return phoneNumber;
    }

    // deprecated
    private String getUniqueID(Context context){
        String uid="";
        uid=loadMacAddress(context);
        if(!uid.equals("")){
            return encryptData(uid).substring(0, 14);
        }

        uid=getMacAddress(context);
        if(uid!=null && uid.length()==12){
            saveMacAddress(context, uid);
            return encryptData(uid).substring(0, 14);
        }

        uid=android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        if(uid!=null && uid.length()==16){
            return uid;
        }

        uid=getPseuedoId(context);
        return uid;
    }

    // deprecated
    private String loadMacAddress(Context context){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("macAddress", "");
    }

    // deprecated
    private void saveMacAddress(Context context, String mac){
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit=prefs.edit();
        edit.putString("macAddress", mac);
        edit.commit();
    }

    private static String getMacAddress(Context context){
        String macAddress=null;
        try{
            WifiManager wfManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wfInfo = wfManager.getConnectionInfo();
            macAddress = wfInfo.getMacAddress();
            //macAddress=macAddress.replaceAll(":", "");
            Log.i("tag", "mac address : " + macAddress);
        }catch (Exception ex){Log.e("tag","get mac address fail ");
            ex.printStackTrace();}
        return macAddress;
    }

    public static String getPseuedoId(Context context){
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
        String rnd=prefs.getString("MyVoteRand", "");
        if(rnd.equals("")){
            rnd=getRandStr(16);
            SharedPreferences.Editor edit=prefs.edit();
            edit.putString("MyVoteRand", rnd);
            edit.commit();
        }
        return rnd;
    }

    private static String getRandStr(int len){
        StringBuffer buf=new StringBuffer();
        for (int i = 0; i < len; i++) {
            int r=(int)(Math.random()*62);
            if(r<26){
                buf.append((char)((Math.random() * 26) + 97));
            }else if(r<52){
                buf.append((char)((Math.random() * 26) + 65));
            }else{
                buf.append((char)((Math.random() * 10) + 48));
            }
        }
        return buf.toString();
    }

    public static String encryptData(String str) {
        StringBuilder sb = new StringBuilder();
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes());
            byte[] md5encrypt = md5.digest();
            for(int i = 0 ; i < md5encrypt.length ; i++){
                sb.append(Integer.toString( (md5encrypt[i] & 0xf0) >> 4, 16) );
                sb.append(Integer.toString(  md5encrypt[i] & 0x0f      , 16) );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
