package ecommerce.app.backend.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.CANADA);
    private final static TimeZone timezone = TimeZone.getTimeZone("GMT-4");

    public static String getNowAsString(){
        simpleDateFormat.setTimeZone(Utils.timezone);
        return simpleDateFormat.format(new Date());
    }

    public static String getDateAsString(Date date){
        simpleDateFormat.setTimeZone(Utils.timezone);
        return simpleDateFormat.format(date);
    }
}
