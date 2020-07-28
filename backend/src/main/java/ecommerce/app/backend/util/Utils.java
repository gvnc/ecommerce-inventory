package ecommerce.app.backend.util;

import lombok.Getter;

import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.CANADA);

    @Getter
    private final static TimeZone timezone = TimeZone.getTimeZone("GMT-4");

    public static String getNowAsString(){
        simpleDateFormat.setTimeZone(Utils.timezone);
        return simpleDateFormat.format(new Date());
    }

    public static String getDateAsString(Date date){
        simpleDateFormat.setTimeZone(Utils.timezone);
        return simpleDateFormat.format(date);
    }

    public static String computeContentMD5Header(String content) {
        DigestInputStream s;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(content.getBytes());
            return new String(org.apache.commons.codec.binary.Base64.encodeBase64(md.digest()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
