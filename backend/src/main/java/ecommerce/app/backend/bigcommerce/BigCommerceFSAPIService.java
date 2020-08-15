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
public class BigCommerceFSAPIService extends BigCommerceBaseService {

    @Autowired
    private StoreBean storeBean;

    public BigCommerceFSAPIService(@Value("${bc.fs.apipath}")String apipath, @Value("${bc.fs.clientid}")String clientId, @Value("${bc.fs.accesstoken}")String accessToken) {
        super(BigCommerceFSAPIService.class, apipath, clientId, accessToken);
    }

    public boolean updatePrice(String productSku, String costPrice, String retailPrice, String price){
        try {
            log.info("Price change request for bigcommerce-fs. [product:"+productSku+",costPrice:"+costPrice+",retailPrice:"+retailPrice+",price:"+price+"]");

            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);
            if(detailedProduct == null)
                return false;

            if(super.updatePrice(detailedProduct.getBigCommerceFSProduct(), productSku, costPrice, retailPrice, price) == true){
                if(detailedProduct.getBigCommerceFSProduct()!= null && price != null){ // update base order price
                    BaseProduct baseProduct = storeBean.getProductsMap().get(productSku);
                    baseProduct.setBigCommerceFSPrice(Float.parseFloat(price));
                }
                return true;
            }
        } catch (Exception e){
            log.error("Error:", e);
        }
        return false;
    }

    @Override
    void updateBaseProduct(String sku, Integer quantity) {
        BaseProduct baseProduct = storeBean.getProductsMap().get(sku);
        if(baseProduct != null){
            baseProduct.setBigCommerceFSInventory(quantity);
        }

    }
}