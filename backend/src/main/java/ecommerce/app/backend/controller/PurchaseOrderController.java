package ecommerce.app.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecommerce.app.backend.amazon.AmazonCaService;
import ecommerce.app.backend.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.model.PurchaseOrderRequest;
import ecommerce.app.backend.repository.PurchaseOrderProductRepository;
import ecommerce.app.backend.repository.PurchaseOrderRepository;
import ecommerce.app.backend.repository.model.PurchaseOrder;
import ecommerce.app.backend.repository.model.PurchaseOrderProduct;
import ecommerce.app.backend.vendhq.VendHQAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200" })
@RequestMapping("/purchase")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderProductRepository purchaseOrderProductRepository;

    @Autowired
    private BigCommerceAPIService bigCommerceAPIService;

    @Autowired
    private BigCommerceFSAPIService bigCommerceFSAPIService;

    @Autowired
    private VendHQAPIService vendHQAPIService;

    @Autowired
    private AmazonCaService amazonCaService;

    @GetMapping("/orders")
    public List<PurchaseOrder> getPurchaseOrders() {
        List<PurchaseOrder> purchaseOrderList = new ArrayList<>();
        try{
            Iterable<PurchaseOrder> iterable = purchaseOrderRepository.findAllByOrderByIdDesc();
            iterable.forEach(purchaseOrderList::add);
        } catch (Exception e){
            log.error("Failed to get purchase orders.", e);
        }
        return  purchaseOrderList;
    }

    @GetMapping("/orders/{orderId}/products")
    public List<PurchaseOrderProduct> getPurchaseOrderProducts(@PathVariable Integer orderId) {
        List<PurchaseOrderProduct> productList = new ArrayList<>();
        try{
            Iterable<PurchaseOrderProduct> iterable = purchaseOrderProductRepository.findAllByPurchaseOrder_Id(orderId);
            iterable.forEach(productList::add);
        } catch (Exception e){
            log.error("Failed to get products for order=" + orderId, e);
        }
        return productList;
    }

    @PostMapping("/orders/create")
    public PurchaseOrder createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        try{
            purchaseOrder.setStatus(PurchaseOrderConstants.DRAFT);
            purchaseOrder.setCreateDate(new Date());
            return purchaseOrderRepository.save(purchaseOrder);
        } catch (Exception e){
            log.error("Failed to save purchase order.", e);
            return null;
        }
    }

    private Float calculateOrderTotal(PurchaseOrder purchaseOrder, List<PurchaseOrderProduct> productList){
        Float orderTotal = 0F;
        if(purchaseOrder != null){
            if(purchaseOrder.getBrokerage() == null)
                purchaseOrder.setBrokerage(0F);

            if(purchaseOrder.getDuties() == null)
                purchaseOrder.setDuties(0F);

            if(purchaseOrder.getSalesTax() == null)
                purchaseOrder.setSalesTax(0F);

            if(purchaseOrder.getShipping() == null)
                purchaseOrder.setShipping(0F);

            if(purchaseOrder.getDiscount() == null)
                purchaseOrder.setDiscount(0F);

            orderTotal = purchaseOrder.getBrokerage() + purchaseOrder.getDuties() +
                    purchaseOrder.getSalesTax() + purchaseOrder.getShipping() - purchaseOrder.getDiscount();
        }
        if(productList != null){
            for (PurchaseOrderProduct product: productList){
                orderTotal = orderTotal + product.getOrderedQuantity() * product.getCostPrice();
            }
        }
        return orderTotal;
    }

    @PostMapping("/orders/{orderId}/save")
    public PurchaseOrderRequest savePurchaseOrder(@PathVariable Integer orderId, @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
        try{
            PurchaseOrder purchaseOrder = purchaseOrderRequest.getPurchaseOrder();
            if(purchaseOrder != null){
                Float orderTotal = calculateOrderTotal(purchaseOrder, purchaseOrderRequest.getProductList());
                purchaseOrder.setOrderTotal(orderTotal);
                purchaseOrderRepository.save(purchaseOrder);
            }
            purchaseOrderRequest.getProductList().stream().forEach(purchaseOrderProduct -> purchaseOrderProduct.setPurchaseOrder(purchaseOrder));
            purchaseOrderProductRepository.saveAll(purchaseOrderRequest.getProductList());
            return purchaseOrderRequest;
        } catch (Exception e){
            log.error("Failed to update purchase order by id " + orderId, e);
            return null;
        }
    }

    @GetMapping("/orders/{orderId}")
    public PurchaseOrderRequest getPurchaseOrder(@PathVariable Integer orderId) {
        try{
            PurchaseOrderRequest po = new PurchaseOrderRequest();

            PurchaseOrder order = purchaseOrderRepository.findById(orderId).orElse(null);
            po.setPurchaseOrder(order);

            List<PurchaseOrderProduct> productList = purchaseOrderProductRepository.findAllByPurchaseOrder_Id(orderId);
            po.setProductList(productList);

            return po;
        } catch (Exception e){
            log.error("Failed to get purchase order by id " + orderId, e);
        }
        return null;
    }

    @DeleteMapping("/orders/{orderId}/products/{productId}")
    public String deletePurchaseOrderProduct(@PathVariable Integer orderId, @PathVariable Integer productId) {
        try{
            purchaseOrderProductRepository.deleteById(productId);

            List<PurchaseOrderProduct> productList = purchaseOrderProductRepository.findAllByPurchaseOrder_Id(orderId);
            PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId).orElse(null);
            Float orderTotal = calculateOrderTotal(purchaseOrder, productList);
            purchaseOrder.setOrderTotal(orderTotal);
            return OperationConstants.SUCCESS;
        } catch (Exception e){
            log.error("Failed to delete purchase order product by order id " + orderId + " and product id " + productId, e);
            return OperationConstants.FAIL;
        }
    }

    @PostMapping("/orders/{orderId}/submit")
    public PurchaseOrderRequest submitPurchaseOrderStatus(@PathVariable Integer orderId, @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
        try{
            PurchaseOrder purchaseOrder = purchaseOrderRequest.getPurchaseOrder();
            if(purchaseOrder != null){
                purchaseOrder.setStatus(PurchaseOrderConstants.SUBMITTED);
                return savePurchaseOrder(orderId, purchaseOrderRequest);
            }
        } catch (Exception e){
            log.error("Failed to submit purchase order by id " + orderId, e);
        }
        return null;
    }

    @PostMapping("/orders/{orderId}/cancel")
    public PurchaseOrderRequest cancelPurchaseOrder(@PathVariable Integer orderId) {
        try{
            PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId).orElse(null);
            if(purchaseOrder != null){
                PurchaseOrderRequest request = new PurchaseOrderRequest();
                purchaseOrder.setStatus(PurchaseOrderConstants.CANCELLED);
                purchaseOrderRepository.save(purchaseOrder);
                request.setPurchaseOrder(purchaseOrder);
                return request;
            }
        } catch (Exception e){
            log.error("Failed to cancel purchase order by id " + orderId, e);
        }
        return null;
    }

    @Transactional
    @DeleteMapping("/orders/{orderId}")
    public String deletePurchaseOrder(@PathVariable Integer orderId) {
        try{
            purchaseOrderProductRepository.deleteByPurchaseOrder_Id(orderId);
            purchaseOrderRepository.deleteById(orderId);
            return OperationConstants.SUCCESS;
        } catch (Exception e){
            log.error("Failed to delete purchase order by id " + orderId, e);
        }
        return OperationConstants.FAIL;
    }

    @PostMapping("/orders/{orderId}/receive")
    public PurchaseOrderRequest receivePurchaseProducts(@PathVariable Integer orderId, @RequestBody ObjectNode requestBody) {
        try{
            // get products first
            List<PurchaseOrderProduct> productList = purchaseOrderProductRepository.findAllByPurchaseOrder_Id(orderId);

            // set received products
            ArrayNode receiveArrayNode = requestBody.withArray("receiveList");
            for(JsonNode receiveNode:receiveArrayNode){
                String sku = receiveNode.get("sku").textValue();
                Integer receivedQuantity = receiveNode.get("receivedQuantity").asInt();
                PurchaseOrderProduct purchaseOrderProduct = productList.stream().filter( p -> p.getSku().equals(sku)).findFirst().orElse(null);
                if(purchaseOrderProduct != null && receivedQuantity > 0) {
                    updateInventories(purchaseOrderProduct.getSku(), receivedQuantity);
                    purchaseOrderProduct.setReceivedQuantity(purchaseOrderProduct.getReceivedQuantity() + receivedQuantity);
                    purchaseOrderProduct.setRemainingQuantity(purchaseOrderProduct.getOrderedQuantity() - purchaseOrderProduct.getReceivedQuantity());
                }
            }

            // set purchase order status
            PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId).orElse(null);
            if(purchaseOrder != null){
                if(productList != null) {
                    boolean remainingFound = productList.stream().anyMatch(purchaseOrderProduct -> purchaseOrderProduct.getRemainingQuantity() > 0);
                    boolean receivedFound = productList.stream().anyMatch(purchaseOrderProduct -> purchaseOrderProduct.getReceivedQuantity() > 0);

                    if (remainingFound == false) {
                        purchaseOrder.setStatus(PurchaseOrderConstants.COMPLETED);
                    } else if (receivedFound == true) {
                        purchaseOrder.setStatus(PurchaseOrderConstants.PARTIAL_RECEIVED);
                    }
                }
            }

            // save changes
            PurchaseOrderRequest purchaseOrderRequest = new PurchaseOrderRequest();
            purchaseOrderRequest.setPurchaseOrder(purchaseOrder);
            purchaseOrderRequest.setProductList(productList);
            return savePurchaseOrder(orderId, purchaseOrderRequest);
        } catch (Exception e){
            log.error("Failed to receive products by order id " + orderId, e);
        }
        return null;
    }

    private void updateInventories(String sku, Integer quantity){
        vendHQAPIService.updateProductQuantity(sku, quantity, false);
        bigCommerceAPIService.updateProductQuantity(sku, quantity, false);
        bigCommerceFSAPIService.updateProductQuantity(sku, quantity, false);
        amazonCaService.updateInventory(sku, quantity, false);
    }
}
