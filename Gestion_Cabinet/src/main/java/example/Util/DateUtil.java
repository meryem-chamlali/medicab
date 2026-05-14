package example.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.sql.Time;

public class DateUtil {

    // ---------------- String -> java.sql.Date ----------------
    public static Date transformerStringEnDate(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setLenient(false); // validation stricte

        try {
            java.util.Date utilDate = formatter.parse(dateStr);
            return new Date(utilDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------------- String -> java.sql.Time ----------------
    public static Time transformerStringEnTime(String timeStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        formatter.setLenient(false);

        try {
            java.util.Date utilDate = formatter.parse(timeStr);
            return new Time(utilDate.getTime()); // convertir en java.sql.Time
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------------- java.sql.Date -> String ----------------
    public static String transformerDateEnString(Date date) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    // ---------------- java.sql.Time -> String ----------------
    public static String transformerTimeEnString(Time time) {
        if (time == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(time);
    }
}