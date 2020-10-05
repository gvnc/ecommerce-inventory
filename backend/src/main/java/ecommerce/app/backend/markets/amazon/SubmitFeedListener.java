package ecommerce.app.backend.markets.amazon;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.products.AmazonProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class SubmitFeedListener {
    @Autowired
    private StoreBean storeBean;

    @Autowired
    private AmazonCaService amazonCaService;

    private boolean priceUpdateInProgressForCA = false;
    private boolean inventoryUpdateInProgressForCA = false;

    @Scheduled(fixedDelayString = "${submit.feed.price.interval}")
    public void updatePriceForAmazonCA(){
        // if running already, skip this turn
        if(priceUpdateInProgressForCA == true)
            return;

        // if there are no changes, return
        Set<String> skuSet = storeBean.getAmazonCaPriceUpdateSet();
        if(skuSet.size() == 0)
            return;

        // set to true to avoid duplicate work
        priceUpdateInProgressForCA = true;

        try {
            List<AmazonProduct> productList = new ArrayList<>();
            skuSet.stream().forEach(sku -> productList.add(storeBean.getDetailedProductsMap().get(sku).getAmazonCaProduct()));
            productList.stream().forEach(amazonProduct -> skuSet.remove(amazonProduct.getSku()));

            // if update failed, push skus back into queue
            if(amazonCaService.updateProductPrice(productList) == false){
                //productList.stream().forEach(amazonProduct -> storeBean.getAmazonCaPriceUpdateSet().add(amazonProduct.getSku()));
                log.warn("Failed to update prodct price.");
            }
        }catch (Exception e){
            log.error("Scheduler failed to update prices for amazon ca");
        }
        // set to false to let new turns ins
        priceUpdateInProgressForCA = false;
    }

    @Scheduled(fixedDelayString = "${submit.feed.inventory.interval}")
    public void updateInventoryForAmazonCA(){
        // if running already, skip this turn
        if(inventoryUpdateInProgressForCA == true)
            return;

        // if there are no changes, return
        Set<String> skuSet = storeBean.getAmazonCaQuantityUpdateSet();
        if(skuSet.size() == 0)
            return;

        // set to true to avoid duplicate work
        inventoryUpdateInProgressForCA = true;

        try {
            List<AmazonProduct> productList = new ArrayList<>();
            skuSet.stream().forEach(sku -> productList.add(storeBean.getDetailedProductsMap().get(sku).getAmazonCaProduct()));
            productList.stream().forEach(amazonProduct -> skuSet.remove(amazonProduct.getSku()));

            // if update failed, push skus back into queue
            if(amazonCaService.updateProductQuantity(productList) == false) {
                //productList.stream().forEach(amazonProduct -> storeBean.getAmazonCaQuantityUpdateSet().add(amazonProduct.getSku()));
                log.warn("Failed to update product inventory.");
            }
        }catch (Exception e){
            log.error("Scheduler failed to update inventory for amazon ca", e);
        }
        // set to false to let new turns ins
        inventoryUpdateInProgressForCA = false;
    }
}