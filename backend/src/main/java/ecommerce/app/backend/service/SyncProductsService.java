package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.AmazonCaService;
import ecommerce.app.backend.markets.amazon.products.AmazonProduct;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceVariant;
import ecommerce.app.backend.service.constants.SyncConstants;
import ecommerce.app.backend.util.Utils;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.markets.vendhq.products.VendHQInventory;
import ecommerce.app.backend.markets.vendhq.products.VendHQInventoryData;
import ecommerce.app.backend.markets.vendhq.products.VendHQProduct;
import ecommerce.app.backend.markets.vendhq.products.VendHQProductsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        try {
            log.info("Started to get products from BigCommerce");
            storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_INPROGRESS);
            while (page == 1 || productsArray.length > 0) {
                productsArray = bigCommerceAPIService.getProductList(page);
                page++;

                for (BigCommerceProduct bigCommerceProduct : productsArray) {
                    if (bigCommerceProduct.getIsVisible() == true) {
                        visibleProductCounter++;
                    }
                    // get variants if exists
                    if(bigCommerceProduct.getInventoryTracking() != null && bigCommerceProduct.getInventoryTracking().equals("variant")){
                        BigCommerceVariant[] variants = bigCommerceAPIService.getVariants(bigCommerceProduct.getId());
                        for(BigCommerceVariant variant:variants){
                            String sku = variant.getSku();
                            String productName = bigCommerceProduct.getName() + " / ";
                            if(variant.getOptionValues() != null && variant.getOptionValues().length > 0){
                                String optionLabel = variant.getOptionValues()[0].getLabel();
                                productName = productName + optionLabel;
                            }
                            BigCommerceProduct variantProduct = (BigCommerceProduct) bigCommerceProduct.clone();
                            variantProduct.setSku(sku);
                            variantProduct.setName(productName);
                            variantProduct.setVariantId(variant.getId());
                            variantProduct.setInventoryLevel(variant.getInventoryLevel());

                            handleSingleBCProduct(variantProduct);
                            productCounter++;
                        }
                    } else {
                        handleSingleBCProduct(bigCommerceProduct);
                        productCounter++;
                    }
                }
            }
            storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_COMPLETED);
            storeBean.getSyncStatus().setBigCommerceLastUpdate(Utils.getNowAsString());
        } catch (Exception e) {
            storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_FAILED);
            log.error("Failed to sync big commerce products", e);
        }

        log.info(productCounter + " products found in BigCommerce.");
        log.info(visibleProductCounter + " products are visible in BigCommerce.");
    }

    private void handleSingleBCProduct(BigCommerceProduct bigCommerceProduct){
        DetailedProduct detailedProduct;
        BaseProduct baseProduct = storeBean.getProductsMap().get(bigCommerceProduct.getSku());
        if (baseProduct != null) {
            detailedProduct = storeBean.getDetailedProductsMap().get(bigCommerceProduct.getSku());
        } else {
            baseProduct = new BaseProduct(bigCommerceProduct.getSku(), bigCommerceProduct.getName());
            storeBean.getProductsMap().put(bigCommerceProduct.getSku(), baseProduct);
            storeBean.getProductsList().add(baseProduct);

            detailedProduct = new DetailedProduct(bigCommerceProduct.getSku(), bigCommerceProduct.getName());
            storeBean.getDetailedProductsMap().put(bigCommerceProduct.getSku(), detailedProduct);
        }
        baseProduct.setBigCommercePrice(Float.parseFloat(bigCommerceProduct.getRetailPrice()));
        baseProduct.setBigCommerceInventory(bigCommerceProduct.getInventoryLevel());
        detailedProduct.setInventoryLevel(bigCommerceProduct.getInventoryLevel());
        detailedProduct.setBigCommerceProduct(bigCommerceProduct);
    }

    private void syncBigCommerceFS() {

        int page = 1;
        int productCounter = 0;
        int visibleProductCounter = 0;
        BigCommerceProduct[] productsArray = new BigCommerceProduct[0];

        try {
            log.info("Started to get products from BigCommerce FS");
            storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_INPROGRESS);
            while (page == 1 || productsArray.length > 0) {
                productsArray = bigCommerceFSAPIService.getProductList(page);
                page++;

                for (BigCommerceProduct bigCommerceProduct : productsArray) {
                    if (bigCommerceProduct.getIsVisible() == true) {
                        visibleProductCounter++;
                    }

                    // get variants if exists
                    if(bigCommerceProduct.getInventoryTracking() != null && bigCommerceProduct.getInventoryTracking().equals("variant")){
                        BigCommerceVariant[] variants = bigCommerceFSAPIService.getVariants(bigCommerceProduct.getId());
                        for(BigCommerceVariant variant:variants){
                            String sku = variant.getSku();
                            String productName = bigCommerceProduct.getName() + " / ";
                            if(variant.getOptionValues() != null && variant.getOptionValues().length > 0){
                                String optionLabel = variant.getOptionValues()[0].getLabel();
                                productName = productName + optionLabel;
                            }
                            BigCommerceProduct variantProduct = (BigCommerceProduct) bigCommerceProduct.clone();
                            variantProduct.setSku(sku);
                            variantProduct.setName(productName);
                            variantProduct.setVariantId(variant.getId());
                            variantProduct.setInventoryLevel(variant.getInventoryLevel());

                            handleSingleBCFSProduct(variantProduct);
                            productCounter++;
                        }
                    } else {
                        handleSingleBCFSProduct(bigCommerceProduct);
                        productCounter++;
                    }
                }
            }
            storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_COMPLETED);
            storeBean.getSyncStatus().setBigCommerceFSLastUpdate(Utils.getNowAsString());
        } catch (Exception e) {
            storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_FAILED);
            log.error("Failed to sync big commerce fs products", e);
        }

        log.info(productCounter + " products found in BigCommerce FS.");
        log.info(visibleProductCounter + " products are visible in BigCommerce FS.");
    }

    private void handleSingleBCFSProduct(BigCommerceProduct bigCommerceProduct){
        DetailedProduct detailedProduct;
        BaseProduct baseProduct = storeBean.getProductsMap().get(bigCommerceProduct.getSku());
        if (baseProduct != null) {
            detailedProduct = storeBean.getDetailedProductsMap().get(bigCommerceProduct.getSku());
        } else {
            baseProduct = new BaseProduct(bigCommerceProduct.getSku(), bigCommerceProduct.getName());
            storeBean.getProductsMap().put(bigCommerceProduct.getSku(), baseProduct);
            storeBean.getProductsList().add(baseProduct);

            detailedProduct = new DetailedProduct(bigCommerceProduct.getSku(), bigCommerceProduct.getName());
            storeBean.getDetailedProductsMap().put(bigCommerceProduct.getSku(), detailedProduct);
        }
        baseProduct.setBigCommerceFSPrice(Float.parseFloat(bigCommerceProduct.getRetailPrice()));
        baseProduct.setBigCommerceFSInventory(bigCommerceProduct.getInventoryLevel());
        detailedProduct.setInventoryLevel(bigCommerceProduct.getInventoryLevel());
        detailedProduct.setBigCommerceFSProduct(bigCommerceProduct);
    }

    private void syncVendHQ(){

        Map<String, VendHQInventory> inventoryMap = getVendHQInventory();

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

                    VendHQInventory inventory = inventoryMap.get(vendHQProduct.getId());
                    vendHQProduct.setInventory(inventory);

                    if(vendHQProduct.getVariantName() != null){
                        vendHQProduct.setName(vendHQProduct.getVariantName());
                    }

                    DetailedProduct detailedProduct;
                    BaseProduct baseProduct = storeBean.getProductsMap().get(vendHQProduct.getSku());
                    if (baseProduct != null) {
                        detailedProduct = storeBean.getDetailedProductsMap().get(vendHQProduct.getSku());
                        if(detailedProduct.getVendHQProduct() != null){
                            duplicates.add(vendHQProduct.getSku());
                        }
                    } else {
                        baseProduct = new BaseProduct(vendHQProduct.getSku(), vendHQProduct.getName());
                        storeBean.getProductsMap().put(vendHQProduct.getSku(), baseProduct);
                        storeBean.getProductsList().add(baseProduct);

                        detailedProduct = new DetailedProduct(vendHQProduct.getSku(), vendHQProduct.getName());
                        storeBean.getDetailedProductsMap().put(vendHQProduct.getSku(), detailedProduct);
                    }
                    if(inventory != null){
                        baseProduct.setVendHQInventory(inventory.getInventoryLevel());
                        detailedProduct.setInventoryLevel(inventory.getInventoryLevel());
                    }
                    baseProduct.setVendHQPrice(vendHQProduct.getPrice());
                    detailedProduct.setVendHQProduct(vendHQProduct);
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

    private Map<String, VendHQInventory> getVendHQInventory(){

        Map<String, VendHQInventory> inventoryMap = new HashMap<>();

        Long version = 0L;

        try {
            log.info("Started to get inventory from VendHQ");

            while (version != null) {
                VendHQInventoryData vendHQInventoryData = vendHQAPIService.getInventoryList(version);
                if(vendHQInventoryData == null || vendHQInventoryData.getVersion() == null)
                    break;

                version = vendHQInventoryData.getVersion().getMax();

                VendHQInventory[] inventoryArray = vendHQInventoryData.getData();
                for (VendHQInventory vendHQInventory : inventoryArray) {
                    inventoryMap.put(vendHQInventory.getProductId(), vendHQInventory);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get vendhq inventory list", e);
        }

        log.info("Inventory list retrieve completed for VendHQ.");
        return inventoryMap;
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
                    DetailedProduct detailedProduct;
                    BaseProduct baseProduct = storeBean.getProductsMap().get(amazonProduct.getSku());
                    if (baseProduct != null) {
                        detailedProduct = storeBean.getDetailedProductsMap().get(amazonProduct.getSku());
                    } else {
                        baseProduct = new BaseProduct(amazonProduct.getSku(), amazonProduct.getName());
                        storeBean.getProductsMap().put(amazonProduct.getSku(), baseProduct);
                        storeBean.getProductsList().add(baseProduct);

                        detailedProduct = new DetailedProduct(amazonProduct.getSku(), amazonProduct.getName());
                        storeBean.getDetailedProductsMap().put(amazonProduct.getSku(), detailedProduct);
                    }
                    baseProduct.setAmazonCAPrice(amazonProduct.getPrice());
                    baseProduct.setAmazonCAInventory(amazonProduct.getQuantity());
                    detailedProduct.setInventoryLevel(amazonProduct.getQuantity());
                    detailedProduct.setAmazonCaProduct(amazonProduct);
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
            syncAmazonCA();
            syncAmazonUS();
        }
    }
}