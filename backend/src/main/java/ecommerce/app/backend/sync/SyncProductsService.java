package ecommerce.app.backend.sync;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.amazon.AmazonCaService;
import ecommerce.app.backend.amazon.products.AmazonProduct;
import ecommerce.app.backend.util.Utils;
import ecommerce.app.backend.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.vendhq.VendHQAPIService;
import ecommerce.app.backend.vendhq.products.VendHQProduct;
import ecommerce.app.backend.vendhq.products.VendHQProductsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class SyncProductsService {

    @Autowired
    private StoreBean storeBean;

    @Autowired
    private BigCommerceAPIService bigCommerceAPIService;

    @Autowired
    private BigCommerceFSAPIService bigCommerceFSAPIService;

    @Autowired
    private VendHQAPIService vendHQAPIService;

    @Autowired
    private AmazonCaService amazonCaService;

    @Value("${sync.products.enabled}")
    private Boolean syncProductsEnabled;

    private void syncBigCommerce() {

        int page = 1;
        int productCounter = 0;
        int visibleProductCounter = 0;
        BigCommerceProduct[] productsArray = new BigCommerceProduct[0];

        List<String> duplicates = new ArrayList();

        try {
            log.info("Started to get products from BigCommerce");
            storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_INPROGRESS);
            while (page == 1 || productsArray.length > 0) {
                productsArray = bigCommerceAPIService.getProductList(page);
                page++;

                for (BigCommerceProduct bigCommerceProduct : productsArray) {
                    if (bigCommerceProduct.getVisible() == true) {
                        visibleProductCounter++;
                    }
                    if (storeBean.getProductsMap().get(bigCommerceProduct.getSku()) != null) {
                        DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(bigCommerceProduct.getSku());

                        // TODO -delete later
                        if(detailedProduct.getBigCommerceProduct() != null){
                            duplicates.add(bigCommerceProduct.getSku());
                        }

                        detailedProduct.setBigCommerceProduct(bigCommerceProduct);
                    } else {
                        BaseProduct baseProduct = new BaseProduct(bigCommerceProduct.getSku(), bigCommerceProduct.getName());
                        storeBean.getProductsMap().put(bigCommerceProduct.getSku(), baseProduct);
                        storeBean.getProductsList().add(baseProduct);

                        DetailedProduct detailedProduct = new DetailedProduct(bigCommerceProduct.getSku(), bigCommerceProduct.getName());
                        detailedProduct.setBigCommerceProduct(bigCommerceProduct);
                        storeBean.getDetailedProductsMap().put(bigCommerceProduct.getSku(), detailedProduct);
                    }
                    productCounter++;
                }
            }
            storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_COMPLETED);
            storeBean.getSyncStatus().setBigCommerceLastUpdate(Utils.getNowAsString());

            log.info("Duplicates BigCommerce: " + duplicates.toString());
        } catch (Exception e) {
            storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_FAILED);
            log.error("Failed to sync big commerce products", e);
        }

        log.info(productCounter + " products found in BigCommerce.");
        log.info(visibleProductCounter + " products are visible in BigCommerce.");
    }

    private void syncBigCommerceFS() {

        int page = 1;
        int productCounter = 0;
        int visibleProductCounter = 0;
        BigCommerceProduct[] productsArray = new BigCommerceProduct[0];

        List<String> duplicates = new ArrayList();

        try {
            log.info("Started to get products from BigCommerce FS");
            storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_INPROGRESS);
            while (page == 1 || productsArray.length > 0) {
                productsArray = bigCommerceFSAPIService.getProductList(page);
                page++;

                for (BigCommerceProduct bigCommerceProduct : productsArray) {
                    if (bigCommerceProduct.getVisible() == true) {
                        visibleProductCounter++;
                    }
                    if (storeBean.getProductsMap().get(bigCommerceProduct.getSku()) != null) {
                        DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(bigCommerceProduct.getSku());

                        // TODO -delete later
                        if(detailedProduct.getBigCommerceFSProduct() != null){
                            duplicates.add(bigCommerceProduct.getSku());
                        }

                        detailedProduct.setBigCommerceFSProduct(bigCommerceProduct);
                    } else {
                        BaseProduct baseProduct = new BaseProduct(bigCommerceProduct.getSku(), bigCommerceProduct.getName());
                        storeBean.getProductsMap().put(bigCommerceProduct.getSku(), baseProduct);
                        storeBean.getProductsList().add(baseProduct);

                        DetailedProduct detailedProduct = new DetailedProduct(bigCommerceProduct.getSku(), bigCommerceProduct.getName());
                        detailedProduct.setBigCommerceFSProduct(bigCommerceProduct);
                        storeBean.getDetailedProductsMap().put(bigCommerceProduct.getSku(), detailedProduct);
                    }
                    productCounter++;
                }
            }
            storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_COMPLETED);
            storeBean.getSyncStatus().setBigCommerceFSLastUpdate(Utils.getNowAsString());

            log.info("Duplicates BigCommerce FS: " + duplicates.toString());
        } catch (Exception e) {
            storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_FAILED);
            log.error("Failed to sync big commerce fs products", e);
        }

        log.info(productCounter + " products found in BigCommerce FS.");
        log.info(visibleProductCounter + " products are visible in BigCommerce FS.");
    }

    private void syncVendHQ(){

        Long version = 0L;
        int productCounter = 0;
        int activeProductCount = 0;

        List<String> duplicates = new ArrayList();

        try {
            log.info("Started to get products from VendHQ");
            storeBean.getSyncStatus().setVendHQSyncStatus(SyncConstants.SYNC_INPROGRESS);
            while (version != null) {
                VendHQProductsData productsData = vendHQAPIService.getProductList(version);
                if(productsData == null || productsData.getVersion() == null)
                    break;

                version = productsData.getVersion().getMax();

                VendHQProduct[] productsArray = productsData.getData();
                for (VendHQProduct vendHQProduct : productsArray) {
                    if (vendHQProduct.getActive() == true) {
                        activeProductCount++;
                    }
                    if (storeBean.getProductsMap().get(vendHQProduct.getSku()) != null) {
                        DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(vendHQProduct.getSku());

                        // TODO -delete later
                        if(detailedProduct.getVendHQProduct() != null){
                            duplicates.add(vendHQProduct.getSku());
                        }

                        detailedProduct.setVendHQProduct(vendHQProduct);
                    } else {
                        BaseProduct baseProduct = new BaseProduct(vendHQProduct.getSku(), vendHQProduct.getName());
                        storeBean.getProductsMap().put(vendHQProduct.getSku(), baseProduct);
                        storeBean.getProductsList().add(baseProduct);

                        DetailedProduct detailedProduct = new DetailedProduct(vendHQProduct.getSku(), vendHQProduct.getName());
                        detailedProduct.setVendHQProduct(vendHQProduct);
                        storeBean.getDetailedProductsMap().put(vendHQProduct.getSku(), detailedProduct);
                    }
                    productCounter++;
                }
            }
            storeBean.getSyncStatus().setVendHQSyncStatus(SyncConstants.SYNC_COMPLETED);
            storeBean.getSyncStatus().setVendHQLastUpdate(Utils.getNowAsString());

            log.info("Duplicates VendHQ: " + duplicates.toString());
        } catch (Exception e) {
            storeBean.getSyncStatus().setVendHQSyncStatus(SyncConstants.SYNC_FAILED);
            log.error("Failed to sync vendhq products", e);
        }

        log.info(productCounter + " products found in VendHQ.");
        log.info(activeProductCount + " products are active in VendHQ.");
    }

    private void syncAmazonUS(){
        // TODO to be implemented later
        storeBean.getSyncStatus().setAmazonUsStatus(SyncConstants.SYNC_NA);
    }

    private void syncAmazonCA(){
        int productCounter = 0;
        try {
            log.info("Started to get products from AmazonCA");
            storeBean.getSyncStatus().setAmazonCaStatus(SyncConstants.SYNC_INPROGRESS);
            List<AmazonProduct> productList = amazonCaService.getProductList();
            if(productList == null) {
                log.error("Product list is null for AmazonCA.");
                storeBean.getSyncStatus().setAmazonCaStatus(SyncConstants.SYNC_FAILED);
            } else {
                for (AmazonProduct amazonProduct : productList) {
                    if (storeBean.getProductsMap().get(amazonProduct.getSku()) != null) {
                        DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(amazonProduct.getSku());
                        detailedProduct.setAmazonCaProduct(amazonProduct);
                    } else {
                        BaseProduct baseProduct = new BaseProduct(amazonProduct.getSku(), amazonProduct.getName());
                        storeBean.getProductsMap().put(amazonProduct.getSku(), baseProduct);
                        storeBean.getProductsList().add(baseProduct);

                        DetailedProduct detailedProduct = new DetailedProduct(amazonProduct.getSku(), amazonProduct.getName());
                        detailedProduct.setAmazonCaProduct(amazonProduct);
                        storeBean.getDetailedProductsMap().put(amazonProduct.getSku(), detailedProduct);
                    }
                    productCounter++;
                }
                storeBean.getSyncStatus().setAmazonCaStatus(SyncConstants.SYNC_COMPLETED);
            }
            storeBean.getSyncStatus().setAmazonCaLastUpdate(Utils.getNowAsString());

        } catch (Exception e) {
            storeBean.getSyncStatus().setAmazonCaStatus(SyncConstants.SYNC_FAILED);
            log.error("Failed to sync amazonca products", e);
        }

        log.info(productCounter + " products found in AmazonCA.");
    }

    private void setSyncStatusIntoPending(){
        storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setVendHQSyncStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setAmazonUsStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setAmazonCaStatus(SyncConstants.SYNC_INPROGRESS);
    }

    private void resetStore(){
        storeBean.setDetailedProductsMap(new HashMap<>());
        storeBean.setProductsList(new ArrayList<>());
        storeBean.setProductsMap(new HashMap<>());
    }

    public void syncAllMarketPlaces(){
        if(syncProductsEnabled == true) {
            this.setSyncStatusIntoPending();
            this.resetStore();

            syncBigCommerce();
            syncBigCommerceFS();
            syncVendHQ();

            syncAmazonUS();
            syncAmazonCA();
        }
    }
}