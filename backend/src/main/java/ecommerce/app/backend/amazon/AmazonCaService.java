package ecommerce.app.backend.amazon;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.amazon.products.AmazonProduct;
import ecommerce.app.backend.model.DetailedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class AmazonCaService extends AmazonBaseService {

    @Autowired
    private StoreBean storeBean;

    public AmazonCaService(@Value("${aws.ca.accessKeyId}")String accessKeyId,
                           @Value("${aws.ca.secret}")String secretAccessKey,
                           @Value("${aws.ca.serviceUrl}")String serviceUrl,
                           @Value("${aws.ca.merchantId}")String merchantId,
                           @Value("${aws.ca.marketPlaceId}")String marketPlaceId) {
        super(AmazonCaService.class, accessKeyId, secretAccessKey, serviceUrl, merchantId, marketPlaceId);
    }

    public boolean updatePrice(String productSku, String price){
        try {
            log.info("Price change request for amazonca. [product:"+productSku+",price:"+price+"]");
            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);
            if(detailedProduct == null)
                return false;

            AmazonProduct amazonProduct = detailedProduct.getAmazonCaProduct();
            if(amazonProduct == null){
                log.warn("Skip price change, no amazon product found with sku " + productSku);
                return true;
            }

            if(price == null){
                log.warn("Price change request ignored for vendhq. Price values are null.");
                return false;
            }
            amazonProduct.setPrice(Float.parseFloat(price));

            log.info("Price change successful for amazonca. [product:"+productSku+",price:"+price+"]");
            return true;
        } catch (Exception e){
            log.error("Failed to change product price for vendhq.", e);
            return false;
        }
    }
}
