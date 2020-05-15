package ecommerce.app.backend.controller;

import ecommerce.app.backend.model.PurchaseOrderRequest;
import ecommerce.app.backend.repository.PurchaseOrderProductRepository;
import ecommerce.app.backend.repository.PurchaseOrderRepository;
import ecommerce.app.backend.repository.model.PurchaseOrder;
import ecommerce.app.backend.repository.model.PurchaseOrderProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/orders/{orderId}/save")
    public String savePurchaseOrder(@PathVariable Integer orderId, @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
        try{
            PurchaseOrder purchaseOrder = purchaseOrderRequest.getPurchaseOrder();
            if(purchaseOrder != null){
                purchaseOrderRepository.save(purchaseOrder);
            }
            purchaseOrderRequest.getProductList().stream().forEach(purchaseOrderProduct -> purchaseOrderProduct.setPurchaseOrder(purchaseOrder));
            purchaseOrderProductRepository.saveAll(purchaseOrderRequest.getProductList());
            return OperationConstants.SUCCESS;
        } catch (Exception e){
            log.error("Failed to update purchase order by id " + orderId, e);
            return OperationConstants.FAIL;
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
}
