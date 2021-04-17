package ecommerce.app.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecommerce.app.backend.model.PurchaseOrderRequest;
import ecommerce.app.backend.repository.model.BaseOrder;
import ecommerce.app.backend.repository.model.PurchaseOrder;
import ecommerce.app.backend.repository.model.PurchaseOrderAttachment;
import ecommerce.app.backend.repository.model.PurchaseOrderProduct;
import ecommerce.app.backend.service.OrderService;
import ecommerce.app.backend.service.PurchaseOrderService;
import ecommerce.app.backend.service.constants.OrderTypeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = { "http://95.111.250.92:3000", "http://localhost:3000", "http://localhost:4200" }, exposedHeaders = "filename")
@RequestMapping("/purchase")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private OrderService orderService;

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

            // get or save baseOrder to hold inventory movement
            BaseOrder baseOrder = orderService.saveOrder(OrderTypeConstants.PURCHASE_ORDER, orderId.toString(), 0F, new Date(), "Active", OrderTypeConstants.PURCHASE_ORDER);

            // set received products
            ArrayNode receiveArrayNode = requestBody.withArray("receiveList");
            for(JsonNode receiveNode:receiveArrayNode){
                String sku = receiveNode.get("sku").textValue();
                Integer receivedQuantity = receiveNode.get("receivedQuantity").asInt();
                purchaseOrderService.updateSkuInventoryByPO(productList, sku, receivedQuantity);

                PurchaseOrderProduct pop = productList.stream().filter(purchaseOrderProduct -> purchaseOrderProduct.getSku().equals(sku)).findFirst().orElse(null);
                String productName = pop != null ? pop.getName() : "Product Name Unknown";
                orderService.saveOrderItem(sku, productName, receivedQuantity, baseOrder);
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

    @PostMapping("/orders/{orderId}/attachment")
    public String uploadAttachment(@PathVariable Integer orderId, @RequestParam MultipartFile file) {
        if(purchaseOrderService.uploadAttachment(orderId, file) == true)
            return OperationConstants.SUCCESS;
        else
            return OperationConstants.FAIL;
    }

    @GetMapping("/orders/{orderId}/attachment")
    public ResponseEntity<byte[]> getFile(@PathVariable Integer orderId) {

        PurchaseOrderAttachment purchaseOrderAttachment = purchaseOrderService.downloadAttachment(orderId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + purchaseOrderAttachment.getFilename() + "\"")
                .header("filename", purchaseOrderAttachment.getFilename())
                .body(purchaseOrderAttachment.getData());
    }
}
