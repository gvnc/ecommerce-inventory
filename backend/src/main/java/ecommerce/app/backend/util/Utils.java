package ecommerce.app.backend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Getter;

import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.CANADA);
    private final static SimpleDateFormat squareupDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CANADA);

    @Getter
    private final static TimeZone timezone = TimeZone.getTimeZone("GMT-4");

    @Getter
    private final static TimeZone gwTimezone = TimeZone.getTimeZone("GMT+0");

    public static String getNowAsString() {
        simpleDateFormat.setTimeZone(Utils.timezone);
        return simpleDateFormat.format(new Date());
    }

    public static String getDateAsString(Date date) {
        simpleDateFormat.setTimeZone(Utils.timezone);
        return simpleDateFormat.format(date);
    }

    public static String getNowAsSquareupString() {
        squareupDateFormat.setTimeZone(Utils.gwTimezone);
        return squareupDateFormat.format(new Date());
    }

    public static String getDateAsSquareupString(Date date) {
        squareupDateFormat.setTimeZone(Utils.gwTimezone);
        return squareupDateFormat.format(date);
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

    public static Integer getIntFromNode(JsonNode valueNode) {
        if (valueNode == null)
            return -1;
        if (valueNode instanceof TextNode) {
            return Integer.parseInt(valueNode.textValue());
        } else if (valueNode instanceof IntNode) {
            return valueNode.intValue();
        }
        return -1;
    }

    public static String getStringFromNode(JsonNode valueNode) {
        if (valueNode == null)
            return null;
        if (valueNode instanceof TextNode) {
            return valueNode.textValue();
        } else if (valueNode instanceof IntNode) {
            return valueNode.toString();
        } else if (valueNode instanceof DoubleNode) {
            return valueNode.toString();
        }
        return null;
    }

    public static Long dollarToCents(String dollarValue) {
        Double centsz = Double.parseDouble(dollarValue) * 100;
        return centsz.longValue();
    }

    public static Long dollarToCents2(String dollarValue) {
        Float floatValue = Float.parseFloat(dollarValue);
        Float cents = floatValue * 100;
        return cents.longValue();
    }

    public static Float centsToDollar(Long cents) {
        return (float) cents /100;
    }
}
