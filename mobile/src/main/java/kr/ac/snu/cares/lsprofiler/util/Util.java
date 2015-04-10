package kr.ac.snu.cares.lsprofiler.util;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by summer on 4/10/15.
 */
public class Util {

    public static long getTodayTimestamp(){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(new Date());

        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
        c2.set(Calendar.MONTH, c1.get(Calendar.MONTH));
        c2.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);

        return c2.getTimeInMillis();
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
