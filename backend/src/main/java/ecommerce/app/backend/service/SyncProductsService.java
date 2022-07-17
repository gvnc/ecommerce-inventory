package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.MarketType;
import ecommerce.app.backend.markets.amazon.AmazonCaService;
import ecommerce.app.backend.markets.amazon.products.AmazonProduct;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceVariant;
import ecommerce.app.backend.markets.helcim.HelcimAPIService;
import ecommerce.app.backend.markets.helcim.products.HelcimProduct;
import ecommerce.app.backend.markets.squareup.SquareAPIService;
import ecommerce.app.backend.markets.squareup.inventory.SquareInventoryCount;
import ecommerce.app.backend.markets.squareup.inventory.SquareInventoryCounts;
import ecommerce.app.backend.markets.squareup.items.*;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.markets.vendhq.products.VendHQInventory;
import ecommerce.app.backend.markets.vendhq.products.VendHQInventoryData;
import ecommerce.app.backend.markets.vendhq.products.VendHQProduct;
import ecommerce.app.backend.markets.vendhq.products.VendHQProductsData;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.service.constants.SyncConstants;
import ecommerce.app.backend.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SyncProductsService {

    @Autowired
    private ScriptService scriptService;

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

    @Autowired
    private SquareAPIService squareAPIService;

    @Autowired
    private HelcimAPIService helcimAPIService;

    @Value("${master.market.type}")
    private MarketType marketType;

    @Value("${sync.bigcommerce.enabled}")
    private Boolean syncBigCommerceEnabled;

    @Value("${sync.bigcommercefs.enabled}")
    private Boolean syncBigCommerceFsEnabled;

    @Value("${sync.vendhq.enabled}")
    private Boolean syncVendHQEnabled;

    @Value("${sync.amazonca.enabled}")
    private Boolean syncAmazonCAEnabled;

    @Value("${sync.helcim.enabled}")
    private Boolean syncHelcimEnabled;

    private void syncBigCommerce() {

        if(!syncBigCommerceEnabled)
            return;

        int page = 1;
        int productCounter = 0;
        int visibleProductCounter = 0;
        BigCommerceProduct[] productsArray = new BigCommerceProduct[0];

        try {
            log.info("Started to get products from BigCommerce");
            storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_INPROGRESS);

            Map<String,List<BigCommerceVariant>> variantMap = bigCommerceAPIService.getAllVariants();

            while (page == 1 || productsArray.length > 0) {
                productsArray = bigCommerceAPIService.getProductList(page, 2);
                page++;

                for (BigCommerceProduct bigCommerceProduct : productsArray) {
                    if (bigCommerceProduct.getIsVisible() == true) {
                        visibleProductCounter++;
                    }
                    // get variants if exists
                    if(bigCommerceProduct.getInventoryTracking() != null && bigCommerceProduct.getInventoryTracking().equals("variant")){
                        List<BigCommerceVariant> variants = variantMap.get(bigCommerceProduct.getId());
                        if(variants != null) {
                            for (BigCommerceVariant variant : variants) {
                                String sku = variant.getSku();
                                String productName = bigCommerceProduct.getName() + " / ";
                                if (variant.getOptionValues() != null && variant.getOptionValues().length > 0) {
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
        baseProduct.setSupplierCode(bigCommerceProduct.getSupplierCode());
        detailedProduct.setInventoryLevel(bigCommerceProduct.getInventoryLevel());
        detailedProduct.setBigCommerceProduct(bigCommerceProduct);
    }

    private void syncBigCommerceFS() {

        if(!syncBigCommerceFsEnabled)
            return;

        int page = 1;
        int productCounter = 0;
        int visibleProductCounter = 0;
        BigCommerceProduct[] productsArray = new BigCommerceProduct[0];

        try {
            log.info("Started to get products from BigCommerce FS");
            storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_INPROGRESS);

            Map<String,List<BigCommerceVariant>> variantMap = bigCommerceAPIService.getAllVariants();

            while (page == 1 || productsArray.length > 0) {
                productsArray = bigCommerceFSAPIService.getProductList(page, 2);
                page++;

                for (BigCommerceProduct bigCommerceProduct : productsArray) {
                    if (bigCommerceProduct.getIsVisible() == true) {
                        visibleProductCounter++;
                    }

                    // get variants if exists
                    if(bigCommerceProduct.getInventoryTracking() != null && bigCommerceProduct.getInventoryTracking().equals("variant")){
                        List<BigCommerceVariant> variants = variantMap.get(bigCommerceProduct.getId());
                        if(variants != null) {
                            for (BigCommerceVariant variant : variants) {
                                String sku = variant.getSku();
                                String productName = bigCommerceProduct.getName() + " / ";
                                if (variant.getOptionValues() != null && variant.getOptionValues().length > 0) {
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
        baseProduct.setSupplierCode(bigCommerceProduct.getSupplierCode());
        detailedProduct.setInventoryLevel(bigCommerceProduct.getInventoryLevel());
        detailedProduct.setBigCommerceFSProduct(bigCommerceProduct);
    }


    private void syncHelcim(){
        if(!syncHelcimEnabled)
            return;

        try {
            log.info("Started to get products from Helcim");
            storeBean.getSyncStatus().setHelcimSyncStatus(SyncConstants.SYNC_INPROGRESS);
            List<HelcimProduct> productList = helcimAPIService.getProductList();
            for(HelcimProduct helcimProduct:productList){
                DetailedProduct detailedProduct;
                BaseProduct baseProduct = storeBean.getProductsMap().get(helcimProduct.getSku());
                if (baseProduct == null) {
                    baseProduct = new BaseProduct(helcimProduct.getSku(), helcimProduct.getName());
                    baseProduct.setHelcimInventory(helcimProduct.getStock().intValue());
                    baseProduct.setHelcimPrice(helcimProduct.getPrice());

                    storeBean.getProductsMap().put(helcimProduct.getSku(), baseProduct);
                    storeBean.getProductsList().add(baseProduct);

                    detailedProduct = new DetailedProduct(helcimProduct.getSku(), helcimProduct.getName());
                    detailedProduct.setHelcimProduct(helcimProduct);
                    detailedProduct.setInventoryLevel(helcimProduct.getStock().intValue());
                    storeBean.getDetailedProductsMap().put(helcimProduct.getSku(), detailedProduct);
                }
            }

            storeBean.getSyncStatus().setHelcimSyncStatus(SyncConstants.SYNC_COMPLETED);
            storeBean.getSyncStatus().setHelcimLastUpdate(Utils.getNowAsString());

            log.info("{} products found in Helcim.", productList == null ? 0: productList.size());
        } catch (Exception e) {
            storeBean.getSyncStatus().setHelcimSyncStatus(SyncConstants.SYNC_FAILED);
            log.error("Failed to sync helcim products", e);
        }
    }

    private void syncVendHQ(){

        if(!syncVendHQEnabled)
            return;

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
                    if(vendHQAPIService.hasValidOutletId(vendHQInventory.getOutletId()))
                        inventoryMap.put(vendHQInventory.getProductId(), vendHQInventory);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get vendhq inventory list", e);
        }

        log.info("Inventory list retrieve completed for VendHQ.");
        return inventoryMap;
    }


    private void syncAmazonCA(){

        if(!syncAmazonCAEnabled)
            return;

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


    private void syncSquareup(){

        Map<String, Integer> inventoryMap = getSquareInventory();

        String cursor = "initial";
        int productCounter = 0;

        try {
            log.info("Started to get products from Squareup");
            storeBean.getSyncStatus().setSquareupSyncStatus(SyncConstants.SYNC_INPROGRESS);
            while (cursor != null) {
                SquareItems squareItems = squareAPIService.getProductList(cursor);
                cursor = squareItems.getCursor();

                if(squareItems == null || squareItems.getObjects() == null)
                    break;

                for(SquareItemObject squareItemObject: squareItems.getObjects()){
                    SquareItemData itemData = squareItemObject.getItemData();
                    if(itemData != null && itemData.getVariations() != null){
                        for(SquareItemVariation variation:itemData.getVariations()){
                            SquareItemVariationData itemVariationData = variation.getItemVariationData();
                            if(itemVariationData != null){
                                String variationName = itemVariationData.getName();
                                variationName = variationName.equals("Regular") ? itemData.getName() : itemData.getName() + " " + variationName;
                              //  log.info("SKU:" + itemVariationData.getSku() + ", NAME:" + variationName);

                                SquareProduct squareProduct = new SquareProduct();
                                squareProduct.setVariationId(variation.getId());
                                squareProduct.setItemId(itemVariationData.getItemId());
                                squareProduct.setName(variationName);
                                squareProduct.setSku(itemVariationData.getSku());
                                squareProduct.setPrice(Utils.centsToDollar(itemVariationData.getPriceMoney().getAmount()));
                                squareProduct.setInventory(0);

                                DetailedProduct detailedProduct;
                                BaseProduct baseProduct = storeBean.getProductsMap().get(itemVariationData.getSku());
                                if (baseProduct != null) {
                                    detailedProduct = storeBean.getDetailedProductsMap().get(itemVariationData.getSku());
                                } else {
                                    baseProduct = new BaseProduct(itemVariationData.getSku(), variationName);
                                    storeBean.getProductsMap().put(itemVariationData.getSku(), baseProduct);
                                    storeBean.getProductsList().add(baseProduct);

                                    detailedProduct = new DetailedProduct(itemVariationData.getSku(), variationName);
                                    storeBean.getDetailedProductsMap().put(itemVariationData.getSku(), detailedProduct);
                                }

                                Integer inventoryCount = inventoryMap.get(squareProduct.getVariationId());
                                if(inventoryCount != null){
                                    squareProduct.setInventory(inventoryCount);
                                }
                                baseProduct.setSquareInventory(squareProduct.getInventory());
                                detailedProduct.setInventoryLevel(squareProduct.getInventory());

                                baseProduct.setSquarePrice(squareProduct.getPrice());
                                detailedProduct.setSquareProduct(squareProduct);
                                productCounter++;
                            }
                        }
                    }
                }
            }
            storeBean.getSyncStatus().setSquareupSyncStatus(SyncConstants.SYNC_COMPLETED);
            storeBean.getSyncStatus().setSquareupLastUpdate(Utils.getNowAsString());
        } catch (Exception e) {
            storeBean.getSyncStatus().setSquareupSyncStatus(SyncConstants.SYNC_FAILED);
            log.error("Failed to sync squareup products", e);
        }

        log.info(productCounter + " products found in SquareUp.");
    }

    private Map<String, Integer> getSquareInventory(){

        Map<String, Integer> inventoryMap = new HashMap<>();

        String cursor = "initial";

        try {
            log.info("Started to get inventory from SquareUp");

            while (cursor != null) {

                SquareInventoryCounts squareInventoryCounts = squareAPIService.getInventoryList(cursor);
                cursor = squareInventoryCounts.getCursor();

                if(squareInventoryCounts == null || squareInventoryCounts.getCounts() == null)
                    break;

                for (SquareInventoryCount inventoryCount:squareInventoryCounts.getCounts()) {
                    inventoryMap.put(inventoryCount.getCatalogObjectId(), inventoryCount.getQuantity());
                }
            }
        } catch (Exception e) {
            log.error("Failed to get squareup inventory list", e);
        }

        log.info("Inventory list retrieve completed for SquareUp.");
        return inventoryMap;
    }

    private void setSyncStatusIntoPending(){
        storeBean.getSyncStatus().setBigCommerceFSSyncStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setBigCommerceSyncStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setVendHQSyncStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setAmazonUsStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setAmazonCaStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setSquareupSyncStatus(SyncConstants.SYNC_INPROGRESS);
        storeBean.getSyncStatus().setHelcimSyncStatus(SyncConstants.SYNC_INPROGRESS);
    }

    private void resetStore(){
        storeBean.setDetailedProductsMap(new HashMap<>());
        storeBean.setProductsList(new ArrayList<>());
        storeBean.setProductsMap(new HashMap<>());
    }

    public void syncFromMaster(){
        // first sync
        syncAllMarketPlaces();
        // then start compare and update
        storeBean.setOrderListenerAllowed(false);
        setSyncStatusIntoPending();
        switch (marketType){
            case BIGCOMMERCE:
                scriptService.syncMarketsByMasterBigCommerce();
                break;
            case VEND:
                scriptService.syncMarketsByMasterVend();
                break;
            case HELCIM:
                scriptService.syncMarketsByMasterHelcim();
                break;
        }
        storeBean.setOrderListenerAllowed(true);
    }

    public void syncAllMarketPlaces(){
        storeBean.setOrderListenerAllowed(false);

        setSyncStatusIntoPending();
        resetStore();
        syncHelcim();
        syncBigCommerce();
        syncBigCommerceFS();
        syncAmazonCA();

        //syncAmazonUS(); // not implemented yet
        // syncSquareup(); // remove comment out to enable square

        /*
        // this was a one-time sync operation, if required again, open it and run again.
        if(1 == 1){
            scriptService.syncSquareInventory();
        }

         */

        // this was a one-time sync operation, if required again, open it and run again.
      /*  if(1 == 1){
            scriptService.syncVendInventory();
        }

       */
        // this was a one-time sync operation, if required again, open it and run again.
/*
        if(1 == 1){
            scriptService.syncBigCommerceInventoryViaVend();
        }
        */

/*
        if(1 == 1){
            scriptService.syncBCFSViaBC();
        }

 */

        storeBean.setOrderListenerAllowed(true);
    }
}