package ecommerce.app.backend.bigcommerce;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BigCommerceAPIService extends BigCommerceBaseService {

    @Autowired
    private StoreBean storeBean;

    public BigCommerceAPIService(@Value("${bc.apipath}")String apipath, @Value("${bc.clientid}")String clientId, @Value("${bc.accesstoken}")String accessToken) {
        super(BigCommerceAPIService.class, apipath, clientId, accessToken);
    }

    public boolean updatePrice(String productSku, String costPrice, String retailPrice, String price){
        try {
            log.info("Price change request for bigcommerce. [product:"+productSku+",costPrice:"+costPrice+",retailPrice:"+retailPrice+",price:"+price+"]");

            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);
            if(detailedProduct == null)
                return false;

            if(super.updatePrice(detailedProduct.getBigCommerceProduct(), productSku, costPrice, retailPrice, price) == true){
                if(retailPrice != null){ // update base order price
                    BaseProduct baseProduct = storeBean.getProductsMap().get(productSku);
                    baseProduct.setBigCommercePrice(Float.parseFloat(retailPrice));
                }
                return true;
            }
        } catch (Exception e){
            log.error("Error:", e);
        }
        return false;
    }
}