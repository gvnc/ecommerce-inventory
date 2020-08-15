package ecommerce.app.backend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
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

    public static Integer getIntFromNode(JsonNode valueNode){
        if(valueNode == null)
            return -1;
        if(valueNode instanceof TextNode){
            return Integer.parseInt(valueNode.textValue());
        } else if(valueNode instanceof IntNode){
            return valueNode.intValue();
        }
        return -1;
    }

    public static String getStringFromNode(JsonNode valueNode){
        if(valueNode == null)
            return null;
        if(valueNode instanceof TextNode){
            return valueNode.textValue();
        } else if(valueNode instanceof IntNode){
            return valueNode.toString();
        }
        return null;
    }
}
