package kr.ac.snu.cares.lsprofiler.util;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by summer on 15. 5. 21.
 */
public class AlarmTime {
    public static final String TAG = AlarmTime.class.getSimpleName();
    public int hour;
    public int min;

    public AlarmTime(int hour, int min) {
        this.hour = hour;
        this.min = min;
    }

    public Calendar getCallendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (System.currentTimeMillis() > cal.getTimeInMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return cal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlarmTime alarmTime = (AlarmTime) o;

        if (hour != alarmTime.hour) return false;
        return min == alarmTime.min;
    }

    @Override
    public int hashCode() {
        int result = hour;
        result = 60 * result + min;
        return result;
    }

    // r>0 : alarmTime1 is after then alarmTime2
    // 0 : equal
    public static int compare(AlarmTime alarmTime1, AlarmTime alarmTime2)
    {
        if (alarmTime1 == null || alarmTime2 == null) {
            Log.e(TAG, "alarmTime1 " + alarmTime1 + " alarmTime2 " + alarmTime2);
            return 0;
        }
        return (int)(alarmTime1.getCallendar().getTimeInMillis() - alarmTime2.getCallendar().getTimeInMillis());
    }
}
