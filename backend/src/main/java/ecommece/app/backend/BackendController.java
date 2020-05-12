package ecommece.app.backend;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ecommece.app.backend.bigcommerce.BigCommerceAPIService;
import ecommece.app.backend.bigcommerce.BigCommerceFSAPIService;
import ecommece.app.backend.bigcommerce.products.BigCommerceProduct;
import ecommece.app.backend.model.*;
import ecommece.app.backend.vendhq.VendHQAPIService;
import ecommece.app.backend.vendhq.products.VendHQInventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200" })
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
        DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);

        // if vendhq product not null, get inventory via apiservice
        if(detailedProduct.getVendHQProduct() != null){
            VendHQInventory inventory = vendHQAPIService.getProductInventoryById(detailedProduct.getVendHQProduct().getId());
            detailedProduct.getVendHQProduct().setInventory(inventory);
            if(inventory != null)
                detailedProduct.setInventoryLevel(inventory.getInventoryLevel());
        }

        // if bigcommerce fs product not null, get inventory via apiservice
        if(detailedProduct.getBigCommerceFSProduct() != null){
            BigCommerceProduct bigCommerceProduct = bigCommerceFSAPIService.getProductBySku(productSku);
            detailedProduct.setBigCommerceFSProduct(bigCommerceProduct);
            detailedProduct.setInventoryLevel(bigCommerceProduct.getInventoryLevel());
        }

        // if bigcommerce product not null, get inventory via apiservice
        if(detailedProduct.getBigCommerceProduct() != null){
             BigCommerceProduct bigCommerceProduct = bigCommerceAPIService.getProductBySku(productSku);
             detailedProduct.setBigCommerceProduct(bigCommerceProduct);
             detailedProduct.setInventoryLevel(bigCommerceProduct.getInventoryLevel());
        }

        return detailedProduct;
    }

    @PostMapping("/products/{productSku}/changePrice")
    public CommitPriceResult updateProductPrice(@PathVariable String productSku, @RequestBody ObjectNode propertyChanges) {

        CommitPriceResult commitPriceResult = new CommitPriceResult();
        commitPriceResult.setFinalResult(OperationConstants.SUCCESS);

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

        return commitPriceResult;
    }

    @PostMapping("/products/{productSku}/updateInventory")
    public InventoryUpdateResult updateProductInventory(@PathVariable String productSku, @RequestBody ObjectNode requestBody) {

        InventoryUpdateResult inventoryUpdateResult = new InventoryUpdateResult();
        inventoryUpdateResult.setFinalResult(OperationConstants.SUCCESS);
        int inventoryLevel = Integer.parseInt(requestBody.get("inventory").textValue());

        if(bigCommerceAPIService.updateProductQuantity(productSku, inventoryLevel, true) == true){
            inventoryUpdateResult.setBigCommerceInventoryUpdate(OperationConstants.SUCCESS);
        } else {
            inventoryUpdateResult.setBigCommerceInventoryUpdate(OperationConstants.FAIL);
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
        }

        if(bigCommerceFSAPIService.updateProductQuantity(productSku, inventoryLevel, true) == true){
            inventoryUpdateResult.setBigCommerceFSInventoryUpdate(OperationConstants.SUCCESS);
        } else {
            inventoryUpdateResult.setBigCommerceFSInventoryUpdate(OperationConstants.FAIL);
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
        }

        if(vendHQAPIService.updateProductQuantity(productSku, inventoryLevel, true)  == true){
            inventoryUpdateResult.setVendhqInventoryUpdate(OperationConstants.SUCCESS);
        } else {
            inventoryUpdateResult.setVendhqInventoryUpdate(OperationConstants.FAIL);
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
        }

        return inventoryUpdateResult;
    }
}