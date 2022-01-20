package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.markets.squareup.SquareAPIService;
import ecommerce.app.backend.markets.squareup.items.SquareProduct;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.markets.vendhq.products.VendHQProduct;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.service.constants.SyncConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ScriptService {

    @Value("${script.enabled:false}")
    private boolean scriptEnabled;

    @Autowired
    private StoreBean storeBean;

    @Autowired
    private SquareAPIService squareAPIService;

    @Autowired
    private VendHQAPIService vendHQAPIService;

    @Autowired
    private BigCommerceAPIService bigCommerceAPIService;

    @Autowired
    private BigCommerceFSAPIService bigCommerceFSAPIService;

    public void syncSquareInventory(){
        if(scriptEnabled == false)
            return;
        Map<String, BaseProduct> productMap = storeBean.getProductsMap();
        for(String sku:productMap.keySet()){

            try {
                BaseProduct baseProduct = productMap.get(sku);
                Integer bcQuantity = baseProduct.getBigCommerceInventory();
                if (bcQuantity == null)
                    bcQuantity = 0;

                Integer sqQuantity = baseProduct.getSquareInventory();
                if (sqQuantity == null)
                    sqQuantity = 0;

                if (sqQuantity.intValue() != bcQuantity.intValue()) {
                    log.info("Difference found. SKU=" + sku + ", BigC=" + bcQuantity + ", Square=" + sqQuantity);
                    SquareProduct squareProduct = storeBean.getDetailedProductsMap().get(sku).getSquareProduct();
                    if (squareProduct != null) {
                        squareAPIService.updateInventory(squareProduct, bcQuantity);
                    } else {
                        log.info("SquareProduct not found for sku " + sku);
                    }
                } else {
                    int justChek = 1;
                }
            }catch (Exception e){
                log.error("Error", e);
            }
        }
    }

    // TODO - not complete !!!
    public void syncVendInventory(){
        if(scriptEnabled == false)
            return;
        Map<String, BaseProduct> productMap = storeBean.getProductsMap();
        for(String sku:productMap.keySet()){

            try {
                BaseProduct baseProduct = productMap.get(sku);
                Integer bcQuantity = baseProduct.getBigCommerceInventory();
                if (bcQuantity == null)
                    bcQuantity = 0;

                Integer vendQuantity = baseProduct.getVendHQInventory();
                if (vendQuantity == null)
                    vendQuantity = 0;

                if (vendQuantity.intValue() != bcQuantity.intValue()) {
                    log.info("Difference found. SKU=" + sku + ", BigC=" + bcQuantity + ", Vend=" + vendQuantity);

                    VendHQProduct vendHQProduct = storeBean.getDetailedProductsMap().get(sku).getVendHQProduct();
                    if (vendHQProduct != null) {
                    //    vendHQAPIService.updateInventory(vendHQProduct, bcQuantity);
                    } else {
                        log.info("Vend product not found for sku " + sku);
                    }
                } else {
                    int justChek = 1;
                }
            }catch (Exception e){
                log.error("Error", e);
            }
        }
    }

    public void syncBigCommerceInventoryViaVend(){
        if(scriptEnabled == false)
            return;

        Map<String, BaseProduct> productMap = storeBean.getProductsMap();
        for(String sku:productMap.keySet()){
            try {
                BigCommerceProduct bigCommerceProduct = storeBean.getDetailedProductsMap().get(sku).getBigCommerceProduct();
                VendHQProduct vendProduct = storeBean.getDetailedProductsMap().get(sku).getVendHQProduct();
                if (bigCommerceProduct != null && vendProduct != null) {

                    BaseProduct baseProduct = productMap.get(sku);
                    Integer bcQuantity = baseProduct.getBigCommerceInventory();
                    if (bcQuantity == null)
                        bcQuantity = 0;

                    Integer vendQuantity = baseProduct.getVendHQInventory();
                    if (vendQuantity == null)
                        vendQuantity = 0;

                    if (vendQuantity.intValue() != bcQuantity.intValue()) {
                        if (vendQuantity.intValue() < 0) {
                            log.warn("vendProduct has negative inventory, skip to update bigcommerce. SKU=" + sku + ", BigC=" + bcQuantity + ", Vend=" + vendQuantity);
                        } else {
                            log.info("Difference found. SKU=" + sku + ", BigC=" + bcQuantity + ", Vend=" + vendQuantity);
                            boolean result = bigCommerceAPIService.updateProductQuantity(bigCommerceProduct, sku, vendQuantity.intValue(), true);
                            if (result == false) {
                                Thread.sleep(30000);
                                bigCommerceAPIService.updateProductQuantity(bigCommerceProduct, sku, vendQuantity.intValue(), true);
                            }
                        }
                    }
                }
            }catch (Exception e){
                log.error("Error", e);
            }
        }

        storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_COMPLETED);
        storeBean.getSyncStatus().setVendHQSyncStatus(SyncConstants.SYNC_COMPLETED);
    }

    public void syncBCFSViaBC(){
        if(scriptEnabled == false)
            return;
        Map<String, BaseProduct> productMap = storeBean.getProductsMap();
        for(String sku:productMap.keySet()){
            try {

                BigCommerceProduct bigCommerceProduct = storeBean.getDetailedProductsMap().get(sku).getBigCommerceProduct();
                BigCommerceProduct bigCommerceFSProduct = storeBean.getDetailedProductsMap().get(sku).getBigCommerceFSProduct();

                if (bigCommerceProduct != null && bigCommerceFSProduct != null) {

                    BaseProduct baseProduct = productMap.get(sku);
                    Integer bcQuantity = baseProduct.getBigCommerceInventory();
                    if (bcQuantity == null)
                        bcQuantity = 0;

                    Integer bcFsQuantity = baseProduct.getBigCommerceFSInventory();
                    if (bcFsQuantity == null)
                        bcFsQuantity = 0;

                    if (bcFsQuantity.intValue() != bcQuantity.intValue()) {

                        if(bcQuantity.intValue() < 0){
                            log.warn("bigcommerce has negative inventory, skip to update bigcommerce fs. SKU=" + sku + ", BigC=" + bcQuantity + ", Vend=" + bcFsQuantity);
                        } else {

                            log.info("Difference found. SKU=" + sku + ", BigC=" + bcQuantity + ", BigCFS=" + bcFsQuantity);
                            boolean result = bigCommerceFSAPIService.updateProductQuantity(bigCommerceFSProduct, sku, bcQuantity.intValue(), true);

                            if (result == false) {
                                log.error("will retry after 30 seconds");
                                Thread.sleep(30000);
                                bigCommerceFSAPIService.updateProductQuantity(bigCommerceFSProduct, sku, bcQuantity.intValue(), true);
                            }
                        }
                    }
                }
            }catch (Exception e){
                log.error("Error", e);
            }
        }
    }

}
