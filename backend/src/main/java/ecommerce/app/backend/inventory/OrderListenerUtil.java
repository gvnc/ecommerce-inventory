package ecommerce.app.backend.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Slf4j
public class OrderListenerUtil {

    private static LatestOrderInfo init(){
        LatestOrderInfo info = new LatestOrderInfo();
        info.setVendMaxVersion(17500012555L);
        info.setBcOrderLastModifiedDate(new Date());
        info.setBcFsOrderLastModifiedDate(new Date());
        info.setAmazonCaLastUpdate(new Date());
        info.setSquareLastModifiedDate(new Date());
        info.setHelcimLastModifiedDate(new Date());

        return info;
    }

    public static LatestOrderInfo getLatestOrderInfo(String filePath){
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(filePath);
            LatestOrderInfo latestOrderInfo = mapper.readValue(file, LatestOrderInfo.class);
            if(latestOrderInfo.getBcOrderLastModifiedDate() == null)
                latestOrderInfo.setBcOrderLastModifiedDate(new Date());
            if(latestOrderInfo.getBcFsOrderLastModifiedDate() == null)
                latestOrderInfo.setBcFsOrderLastModifiedDate(new Date());
            if(latestOrderInfo.getAmazonCaLastUpdate() == null)
                latestOrderInfo.setAmazonCaLastUpdate(new Date());
            if(latestOrderInfo.getSquareLastModifiedDate() == null)
                latestOrderInfo.setSquareLastModifiedDate(new Date());
            if(latestOrderInfo.getVendMaxVersion() == null)
                latestOrderInfo.setVendMaxVersion(17500012555L);
            if(latestOrderInfo.getHelcimLastModifiedDate() == null)
                latestOrderInfo.setHelcimLastModifiedDate(new Date());
            return latestOrderInfo;
        } catch (IOException e) {
            log.error("Can not read latest order info from file. " + filePath, e);
            return init();
        }
    }

    public static boolean saveLatestOrderInfo(String filePath, LatestOrderInfo latestOrderInfo){
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(filePath);
            mapper.writeValue(file, latestOrderInfo);
            return true;
        } catch (IOException e) {
            log.error("Can not save latest order info to file. " + filePath, e);
            return false;
        }
    }
}
