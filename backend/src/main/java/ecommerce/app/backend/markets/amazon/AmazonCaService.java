package ecommerce.app.backend.markets.amazon;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.products.AmazonProduct;
import ecommerce.app.backend.inventory.TestProducts;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AmazonCaService extends AmazonBaseService {

    @Autowired
    private StoreBean storeBean;

    @Autowired
    private TestProducts testProducts;

    @Value("${price.update.enabled:true}")
    private boolean priceUpdateEnabled;

    public AmazonCaService(@Value("${aws.ca.accessKeyId}")String accessKeyId,
                           @Value("${aws.ca.secret}")String secretAccessKey,
                           @Value("${aws.ca.serviceUrl}")String serviceUrl,
                           @Value("${aws.ca.merchantId}")String merchantId,
                           @Value("${aws.ca.marketPlaceId}")String marketPlaceId) {
        super(AmazonCaService.class, accessKeyId, secretAccessKey, serviceUrl, merchantId, marketPlaceId);
    }

    public boolean updatePrice(String productSku, String price){
        try {
            log.info("Price change request for amazon ca. [product:"+productSku+",price:"+price+"]");
            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);
            if(detailedProduct == null)
                return false;

            AmazonProduct amazonProduct = detailedProduct.getAmazonCaProduct();
            if(amazonProduct == null){
                log.warn("Skip price change, no amazon product found with sku " + productSku);
                return true;
            }

            if(price == null){
                log.warn("Price change request ignored for amazon ca. Price values are null.");
                return false;
            }
            if(priceUpdateEnabled) {
                storeBean.getAmazonCaPriceUpdateSet().add(detailedProduct.getSku());
            }
            amazonProduct.setPrice(Float.parseFloat(price));

            BaseProduct baseProduct = storeBean.getProductsMap().get(productSku);
            baseProduct.setAmazonCAPrice(Float.parseFloat(price));

            log.info("Price change successful for amazon ca. [product:"+productSku+",price:"+price+"]");
            return true;
        } catch (Exception e){
            log.error("Failed to change product price for amazon ca.", e);
            return false;
        }
    }

    public boolean updateInventory(String productSku, Integer quantity, Boolean overwrite){
        if(!testProducts.isAvailable(productSku)){
            return true;
        }
        try {
            log.info("Inventory update request for amazon ca. [product:"+productSku+",quantity:"+quantity+"]");
            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);
            if(detailedProduct == null)
                return false;

            AmazonProduct amazonProduct = detailedProduct.getAmazonCaProduct();
            if(amazonProduct == null){
                log.warn("Skip inventory change, no amazon product found with sku " + productSku);
                return true;
            }

            if(quantity == null){
                log.warn("Inventory change request ignored for amazon ca. Quantity value is null.");
                return false;
            }

            int currentQuantity = amazonProduct.getQuantity();
            int newQuantity = currentQuantity + quantity;
            if(overwrite == true){
                newQuantity = quantity;
            }
            if (newQuantity < 0) {
                log.warn("There is no enough inventory in the amazon ca store for sku " + productSku + ". [currentQuantity:" + currentQuantity + ", demanded:" + quantity);
                log.warn("Set inventory to 0 for sku " + productSku);
                newQuantity = 0;
            }
            // just change it in memory
            amazonProduct.setQuantity(newQuantity);

            // update in base product
            BaseProduct baseProduct = storeBean.getProductsMap().get(productSku);
            baseProduct.setAmazonCAInventory(newQuantity);

            // set product sku in update set to let listener change it
            storeBean.getAmazonCaQuantityUpdateSet().add(detailedProduct.getSku());

            log.info("Inventory change successful for amazon ca. [product:"+productSku+",quantity:"+quantity+"]");
            return true;
        } catch (Exception e){
            log.error("Failed to change product quantity for amazon ca.", e);
            return false;
        }
    }

}
