package ecommerce.app.backend.controller;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.amazon.AmazonCaService;
import ecommerce.app.backend.amazon.products.AmazonProduct;
import ecommerce.app.backend.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.model.*;
import ecommerce.app.backend.sync.SyncProductsService;
import ecommerce.app.backend.vendhq.VendHQAPIService;
import ecommerce.app.backend.vendhq.products.VendHQInventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@CrossOrigin(origins = { "http://95.111.250.92:3000", "http://localhost:3000", "http://localhost:4200" })
@RestController
public class BackendController {

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
    private SyncProductsService syncProductsService;

    @GetMapping("/isAppRunning")
    public Boolean isAppRunning() {
        return true;
    }

    @GetMapping("/syncStatus")
    public SyncStatus getSyncStatus() {
        return storeBean.getSyncStatus();
    }

    @GetMapping("/startSync")
    public SyncStatus startSync() {
        syncProductsService.syncAllMarketPlaces();
        return storeBean.getSyncStatus();
    }

    @GetMapping("/getOrders")
    public List<BaseOrder> getOrders() {
        return storeBean.getOrderStatusChanges();
    }

    @GetMapping("/products/list")
    public List<BaseProduct> getProductsList() {
        return storeBean.getProductsList();
    }

    @GetMapping("/products/{productSku}")
    public DetailedProduct getDetailedProduct(@PathVariable String productSku) {
        // became same method with below after deleting instant inventory queries
        return storeBean.getDetailedProductsMap().get(productSku);
    }

    @GetMapping("/products/minimum/{productSku}")
    public DetailedProduct getDetailedProductMinimum(@PathVariable String productSku) {
        return storeBean.getDetailedProductsMap().get(productSku);
    }

    @GetMapping("/products/skuList")
    public List<DetailedProduct> getDetailedProductsBySkuList(@RequestParam String skuList) {
        List<DetailedProduct> productList = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(skuList, ",");
        while (tokenizer.hasMoreTokens()){
            String sku = tokenizer.nextToken();
            DetailedProduct detailedProduct = getDetailedProductMinimum(sku);
            if(detailedProduct != null)
                productList.add(detailedProduct);
        }
        return productList;
    }

    @PostMapping("/products/{productSku}/changePrice")
    public CommitPriceResult updateProductPrice(@PathVariable String productSku, @RequestBody ObjectNode propertyChanges) {

        CommitPriceResult commitPriceResult = new CommitPriceResult();
        commitPriceResult.setFinalResult(OperationConstants.SUCCESS);

        String marketPlace = propertyChanges.get("marketPlace") == null? null : propertyChanges.get("marketPlace").textValue();
        if(marketPlace != null){
            if(marketPlace.equals("BigCommerce")){

                String newCostPrice = propertyChanges.get("bigCommerceCostPrice") == null? null : propertyChanges.get("bigCommerceCostPrice").textValue();
                String newRetailPrice = propertyChanges.get("bigCommerceRetailPrice") == null? null : propertyChanges.get("bigCommerceRetailPrice").textValue();
                String newPrice = propertyChanges.get("bigCommercePrice") == null? null : propertyChanges.get("bigCommercePrice").textValue();

                // update bigcommerce product inventory
                if(bigCommerceAPIService.updatePrice(productSku, newCostPrice, newRetailPrice, newPrice) == true){
                    commitPriceResult.setBigCommercePriceChange(OperationConstants.SUCCESS);
                } else {
                    commitPriceResult.setBigCommercePriceChange(OperationConstants.FAIL);
                    commitPriceResult.setFinalResult(OperationConstants.FAIL);
                }

                // update bigcommerce fs product inventory
                if(bigCommerceFSAPIService.updatePrice(productSku, newCostPrice, newRetailPrice, newPrice) == true){
                    commitPriceResult.setBigCommerceFSPriceChange(OperationConstants.SUCCESS);
                } else {
                    commitPriceResult.setBigCommerceFSPriceChange(OperationConstants.FAIL);
                    commitPriceResult.setFinalResult(OperationConstants.FAIL);
                }

                // update vendhq product inventory
                if(vendHQAPIService.updatePrice(productSku, newCostPrice, newRetailPrice) == true){
                    commitPriceResult.setVendhqPriceChange(OperationConstants.SUCCESS);
                } else {
                    commitPriceResult.setVendhqPriceChange(OperationConstants.FAIL);
                    commitPriceResult.setFinalResult(OperationConstants.FAIL);
                }
            } else if (marketPlace.equals("Amazon")){
                String newPrice = propertyChanges.get("amazonPrice") == null? null : propertyChanges.get("amazonPrice").textValue();
                if(amazonCaService.updatePrice(productSku, newPrice) == true){
                    commitPriceResult.setAmazonCaPriceChange(OperationConstants.SUCCESS);
                } else {
                    commitPriceResult.setAmazonCaPriceChange(OperationConstants.FAIL);
                    commitPriceResult.setFinalResult(OperationConstants.FAIL);
                }
            }
        } else {
            commitPriceResult.setFinalResult(OperationConstants.FAIL);
        }
        return commitPriceResult;
    }

    @PostMapping("/products/{productSku}/updateInventory")
    public InventoryUpdateResult updateProductInventory(@PathVariable String productSku, @RequestBody ObjectNode requestBody) {

        InventoryUpdateResult inventoryUpdateResult = new InventoryUpdateResult();
        inventoryUpdateResult.setFinalResult(OperationConstants.SUCCESS);

        int inventoryLevel = -1;
        if(requestBody.get("inventory") instanceof TextNode){
            inventoryLevel = Integer.parseInt(requestBody.get("inventory").textValue());
        } else if(requestBody.get("inventory") instanceof IntNode){
            inventoryLevel = requestBody.get("inventory").intValue();
        } else {
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
            return inventoryUpdateResult;
        }

        DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);

        if(bigCommerceAPIService.updateProductQuantity(detailedProduct.getBigCommerceProduct(), productSku, inventoryLevel, true) == true){
            inventoryUpdateResult.setBigCommerceInventoryUpdate(OperationConstants.SUCCESS);
            detailedProduct.setInventoryLevel(inventoryLevel);
        } else {
            inventoryUpdateResult.setBigCommerceInventoryUpdate(OperationConstants.FAIL);
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
        }

        if(bigCommerceFSAPIService.updateProductQuantity(detailedProduct.getBigCommerceFSProduct(), productSku, inventoryLevel, true) == true){
            inventoryUpdateResult.setBigCommerceFSInventoryUpdate(OperationConstants.SUCCESS);
            detailedProduct.setInventoryLevel(inventoryLevel);
        } else {
            inventoryUpdateResult.setBigCommerceFSInventoryUpdate(OperationConstants.FAIL);
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
        }

        if(vendHQAPIService.updateProductQuantity(detailedProduct.getVendHQProduct(), productSku, inventoryLevel, true)  == true){
            inventoryUpdateResult.setVendhqInventoryUpdate(OperationConstants.SUCCESS);
            detailedProduct.setInventoryLevel(inventoryLevel);
        } else {
            inventoryUpdateResult.setVendhqInventoryUpdate(OperationConstants.FAIL);
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
        }

        if(amazonCaService.updateInventory(productSku, inventoryLevel, true)  == true){
            inventoryUpdateResult.setAmazonCaInventoryUpdate(OperationConstants.SUCCESS);
            detailedProduct.setInventoryLevel(inventoryLevel);
        } else {
            inventoryUpdateResult.setAmazonCaInventoryUpdate(OperationConstants.FAIL);
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
        }

        return inventoryUpdateResult;
    }
}