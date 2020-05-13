package ecommerce.app.backend.controller;

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
    public PurchaseOrder savePurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        try{
            purchaseOrder.setStatus(PurchaseOrderConstants.DRAFT);
            purchaseOrder.setCreateDate(new Date());
            return purchaseOrderRepository.save(purchaseOrder);
        } catch (Exception e){
            log.error("Failed to save purchase order.", e);
            return null;
        }
    }
}
