package ecommerce.app.backend.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.AmazonCaService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.squareup.SquareAPIService;
import ecommerce.app.backend.model.*;
import ecommerce.app.backend.repository.model.AverageCostView;
import ecommerce.app.backend.repository.model.BaseOrder;
import ecommerce.app.backend.service.OrderService;
import ecommerce.app.backend.service.PurchaseOrderService;
import ecommerce.app.backend.service.SyncProductsService;
import ecommerce.app.backend.util.Utils;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
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
    private SquareAPIService squareAPIService;

    @Autowired
    private SyncProductsService syncProductsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

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
        return orderService.getOrdersFor3MonthsBack();
    }

    @GetMapping("/products/list")
    public List<BaseProduct> getProductsList() {
        return storeBean.getProductsList();
    }

    @GetMapping("/products/{productSku}")
    public DetailedProduct getDetailedProduct(@PathVariable String productSku) {
        DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);
        detailedProduct.setAverageCost(0F);
        AverageCostView averageCostView = purchaseOrderService.getAverageCostView(productSku);
        if(averageCostView != null)
            detailedProduct.setAverageCost(averageCostView.getAverageCost());
        return detailedProduct;
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

        String marketPlace = Utils.getStringFromNode(propertyChanges.get("marketPlace"));

        if(marketPlace != null){
            if(marketPlace.equals("BigCommerce")){

                String newCostPrice = Utils.getStringFromNode(propertyChanges.get("bigCommerceCostPrice"));
                String newRetailPrice = Utils.getStringFromNode(propertyChanges.get("bigCommerceRetailPrice"));
                String newPrice = Utils.getStringFromNode(propertyChanges.get("bigCommercePrice"));

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

                // update squareup product inventory
                /* // remove comment out to enable square
                if(squareAPIService.updatePrice(productSku, newRetailPrice) == true){
                    commitPriceResult.setSquarePriceChange(OperationConstants.SUCCESS);
                } else {
                    commitPriceResult.setSquarePriceChange(OperationConstants.FAIL);
                    commitPriceResult.setFinalResult(OperationConstants.FAIL);
                }

                 */
            } else if (marketPlace.equals("Amazon")){

                String newPrice = Utils.getStringFromNode(propertyChanges.get("amazonPrice"));

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

        int inventoryLevel = Utils.getIntFromNode(requestBody.get("inventory"));
        if(inventoryLevel == -1) {
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

        // remove comment out to enable square
        /*
        if(squareAPIService.updateProductQuantity(detailedProduct.getSquareProduct(), productSku, inventoryLevel, true)  == true){
            inventoryUpdateResult.setSquareInventoryUpdate(OperationConstants.SUCCESS);
            detailedProduct.setInventoryLevel(inventoryLevel);
        } else {
            inventoryUpdateResult.setSquareInventoryUpdate(OperationConstants.FAIL);
            inventoryUpdateResult.setFinalResult(OperationConstants.FAIL);
        }

         */

        return inventoryUpdateResult;
    }

    @GetMapping("/products/{productSku}/averageCost")
    public AverageCostView getAverageCostView(@PathVariable String productSku){
        return purchaseOrderService.getAverageCostView(productSku);
    }
}