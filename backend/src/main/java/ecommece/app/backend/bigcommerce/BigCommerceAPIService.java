package ecommece.app.backend.bigcommerce;

import ecommece.app.backend.StoreBean;
import ecommece.app.backend.model.DetailedProduct;
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

            super.updatePrice(detailedProduct.getBigCommerceProduct(), productSku, costPrice, retailPrice, price);

            return true;
        } catch (Exception e){
            return false;
        }
    }
}