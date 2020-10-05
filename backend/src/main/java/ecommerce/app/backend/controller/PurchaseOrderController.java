package ecommerce.app.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecommerce.app.backend.model.PurchaseOrderRequest;
import ecommerce.app.backend.repository.model.PurchaseOrder;
import ecommerce.app.backend.repository.model.PurchaseOrderProduct;
import ecommerce.app.backend.service.PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = { "http://95.111.250.92:3000", "http://localhost:3000", "http://localhost:4200" })
@RequestMapping("/purchase")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping("/orders")
    public List<PurchaseOrder> getPurchaseOrders() {
        return purchaseOrderService.getPurchaseOrders();
    }

    @GetMapping("/orders/{orderId}/products")
    public List<PurchaseOrderProduct> getPurchaseOrderProducts(@PathVariable Integer orderId) {
        return purchaseOrderService.getPurchaseOrderProducts(orderId);
    }

    @PostMapping("/orders/create")
    public PurchaseOrder createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        return purchaseOrderService.createPurchaseOrder(purchaseOrder);
    }

    @PostMapping("/orders/{orderId}/save")
    public PurchaseOrderRequest savePurchaseOrder(@PathVariable Integer orderId, @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
        return purchaseOrderService.savePurchaseOrder(orderId, purchaseOrderRequest);
    }

    @GetMapping("/orders/{orderId}")
    public PurchaseOrderRequest getPurchaseOrder(@PathVariable Integer orderId) {
        return purchaseOrderService.getPurchaseOrderRequest(orderId);
    }

    @DeleteMapping("/orders/{orderId}/products/{productId}")
    public String deletePurchaseOrderProduct(@PathVariable Integer orderId, @PathVariable Integer productId) {
        if(purchaseOrderService.deletePurchaseOrderProduct(orderId, productId) == true)
            return OperationConstants.SUCCESS;
        else
            return OperationConstants.FAIL;
    }

    @PostMapping("/orders/{orderId}/submit")
    public PurchaseOrderRequest submitPurchaseOrderStatus(@PathVariable Integer orderId, @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
        return purchaseOrderService.submitPurchaseOrderStatus(orderId, purchaseOrderRequest);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public PurchaseOrderRequest cancelPurchaseOrder(@PathVariable Integer orderId) {
        return purchaseOrderService.cancelPurchaseOrder(orderId);
    }

    @DeleteMapping("/orders/{orderId}")
    public String deletePurchaseOrder(@PathVariable Integer orderId) {
        if(purchaseOrderService.deletePurchaseOrder(orderId) == true)
            return OperationConstants.SUCCESS;
        else
            return OperationConstants.FAIL;
    }

    @PostMapping("/orders/{orderId}/receive")
    public PurchaseOrderRequest receivePurchaseProducts(@PathVariable Integer orderId, @RequestBody ObjectNode requestBody) {
        try{
            // get products first
            List<PurchaseOrderProduct> productList = purchaseOrderService.getPurchaseOrderProducts(orderId);

            // set received products
            ArrayNode receiveArrayNode = requestBody.withArray("receiveList");
            for(JsonNode receiveNode:receiveArrayNode){
                String sku = receiveNode.get("sku").textValue();
                Integer receivedQuantity = receiveNode.get("receivedQuantity").asInt();
                purchaseOrderService.updateSkuInventoryByPO(productList, sku, receivedQuantity);
            }

            // set purchase order status
            PurchaseOrder purchaseOrder = purchaseOrderService.updatePurchaseOrderStatus(orderId, productList);

            // save purchase order request
            PurchaseOrderRequest purchaseOrderRequest = new PurchaseOrderRequest();
            purchaseOrderRequest.setPurchaseOrder(purchaseOrder);
            purchaseOrderRequest.setProductList(productList);
            return purchaseOrderService.savePurchaseOrder(orderId, purchaseOrderRequest);
        } catch (Exception e){
            log.error("Failed to receive products by order id " + orderId, e);
        }
        return null;
    }
}
