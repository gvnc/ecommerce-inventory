package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.AmazonCaService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.squareup.SquareAPIService;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.model.PurchaseOrderRequest;
import ecommerce.app.backend.repository.PurchaseOrderProductRepository;
import ecommerce.app.backend.repository.PurchaseOrderRepository;
import ecommerce.app.backend.repository.model.PurchaseOrder;
import ecommerce.app.backend.repository.model.PurchaseOrderProduct;
import ecommerce.app.backend.service.constants.PurchaseOrderConstants;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PurchaseOrderService {

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

    @Autowired
    private SquareAPIService squareAPIService;

    @Autowired
    private StoreBean storeBean;

    public List<PurchaseOrder> getPurchaseOrders() {
        List<PurchaseOrder> purchaseOrderList = new ArrayList<>();
        try{
            Iterable<PurchaseOrder> iterable = purchaseOrderRepository.findAllByOrderByIdDesc();
            iterable.forEach(purchaseOrderList::add);
        } catch (Exception e){
            log.error("Failed to get purchase orders.", e);
        }
        return purchaseOrderList;
    }

    public List<PurchaseOrderProduct> getPurchaseOrderProducts(Integer orderId) {
        return purchaseOrderProductRepository.findAllByPurchaseOrder_Id(orderId);
    }

    public PurchaseOrder createPurchaseOrder(PurchaseOrder purchaseOrder) {
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

    public PurchaseOrderRequest savePurchaseOrder(Integer orderId, PurchaseOrderRequest purchaseOrderRequest) {
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

    public PurchaseOrderRequest getPurchaseOrderRequest(Integer orderId) {
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

    public boolean deletePurchaseOrderProduct(Integer orderId, Integer productId) {
        try{
            purchaseOrderProductRepository.deleteById(productId);

            List<PurchaseOrderProduct> productList = purchaseOrderProductRepository.findAllByPurchaseOrder_Id(orderId);
            PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId).orElse(null);
            Float orderTotal = calculateOrderTotal(purchaseOrder, productList);
            purchaseOrder.setOrderTotal(orderTotal);
            return true;
        } catch (Exception e){
            log.error("Failed to delete purchase order product by order id " + orderId + " and product id " + productId, e);
            return false;
        }
    }

    public PurchaseOrderRequest submitPurchaseOrderStatus(Integer orderId, PurchaseOrderRequest purchaseOrderRequest) {
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

    public PurchaseOrderRequest cancelPurchaseOrder(Integer orderId) {
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
    public boolean deletePurchaseOrder(Integer orderId) {
        try{
            purchaseOrderProductRepository.deleteByPurchaseOrder_Id(orderId);
            purchaseOrderRepository.deleteById(orderId);
            return true;
        } catch (Exception e){
            log.error("Failed to delete purchase order by id " + orderId, e);
        }
        return false;
    }

    public void updateSkuInventoryByPO(List<PurchaseOrderProduct> productList, String sku, Integer quantity){
        PurchaseOrderProduct purchaseOrderProduct = productList.stream().filter( p -> p.getSku().equals(sku)).findFirst().orElse(null);
        if(purchaseOrderProduct != null && quantity > 0) {
            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(sku);

            // set overall quantity
            int newQuantity = quantity;
            if (detailedProduct.getInventoryLevel() != null)
                newQuantity = detailedProduct.getInventoryLevel() + quantity;
            detailedProduct.setInventoryLevel(newQuantity);

            // set quantities for each market place
            vendHQAPIService.updateProductQuantity(detailedProduct.getVendHQProduct(), sku, quantity, false);
            bigCommerceAPIService.updateProductQuantity(detailedProduct.getBigCommerceProduct(), sku, quantity, false);
            bigCommerceFSAPIService.updateProductQuantity(detailedProduct.getBigCommerceFSProduct(), sku, quantity, false);
            amazonCaService.updateInventory(sku, quantity, false);
            squareAPIService.updateProductQuantity(detailedProduct.getSquareProduct(), sku, quantity, false);

            // set received and remaining quantity in purchase order product
            purchaseOrderProduct.setReceivedQuantity(purchaseOrderProduct.getReceivedQuantity() + quantity);
            purchaseOrderProduct.setRemainingQuantity(purchaseOrderProduct.getOrderedQuantity() - purchaseOrderProduct.getReceivedQuantity());
        }
    }

    public PurchaseOrder updatePurchaseOrderStatus (Integer orderId, List<PurchaseOrderProduct> productList){
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
        return purchaseOrder;
    }
}
