package ecommerce.app.backend.inventory;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.bigcommerce.order.BCOrder;
import ecommerce.app.backend.bigcommerce.order.BCOrderProduct;
import ecommerce.app.backend.bigcommerce.order.BCOrderStatuses;
import ecommerce.app.backend.model.BaseOrder;
import ecommerce.app.backend.vendhq.VendHQAPIService;
import ecommerce.app.backend.vendhq.products.VendHQProduct;
import ecommerce.app.backend.vendhq.sales.VendHQSale;
import ecommerce.app.backend.vendhq.sales.VendHQSalesProduct;
import ecommerce.app.backend.vendhq.sales.VendHQSalesStatuses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderListener {

    private LatestOrderInfo latestOrderInfo;

    @Autowired
    private BigCommerceAPIService bigCommerceAPIService;

    @Autowired
    private BigCommerceFSAPIService bigCommerceFSAPIService;

    @Autowired
    private VendHQAPIService vendHQAPIService;

    @Autowired
    private StoreBean storeBean;

    @Value("${order.listener.enabled}")
    private Boolean orderListenerEnabled;

    private String orderListenerDataFile;

    public OrderListener(@Value("${order.listener.data.file}")String orderListenerDataFile) {
        this.orderListenerDataFile = orderListenerDataFile;
        this.latestOrderInfo = OrderListenerUtil.getLatestOrderInfo(orderListenerDataFile);
    }

    @Scheduled(fixedDelay = 30000)
    public void listenInventoryChanges(){

        if(orderListenerEnabled == true){
            // TODO if sync is in progress do nothing !!
            log.info("Order listener started to run.");
            listenBigCommerceOrders();
            listenVendHQSales();
            listenBigCommerceFSOrders();
            OrderListenerUtil.saveLatestOrderInfo(orderListenerDataFile, latestOrderInfo);
            log.info("Order listener ended running.");
        }
    }

    public void listenBigCommerceOrders(){
        log.info("Started to check bigcommerce orders.");
        try {
            List<BCOrder> ordersList = bigCommerceAPIService.getOrders();
            if(ordersList != null){
                for(BCOrder order :ordersList){
                    if(order.getModifiedDate().after(latestOrderInfo.getBcOrderLastModifiedDate())){
                        log.info("An order event is captured for BigCommerce. [orderId:" + order.getId() + ",status:" + order.getStatus() + "]");
                        BaseOrder baseOrder = new BaseOrder("BigCommerce", order.getId(), order.getTotalIncludingTax(), order.getModifiedDate(), order.getStatus());
                        storeBean.getOrderStatusChanges().add(0,baseOrder);
                        if(order.getStatus().equals(BCOrderStatuses.AWAITING_FULFILLMENT)){
                            updateInventoriesByBigCommerce(order.getId(), InventoryChange.DECREASE);
                        } else if (order.getStatus().equals(BCOrderStatuses.CANCELLED) ||
                                order.getStatus().equals(BCOrderStatuses.DECLINED) ||
                                order.getStatus().equals(BCOrderStatuses.REFUNDED)){
                            updateInventoriesByBigCommerce(order.getId(), InventoryChange.INCREASE);
                        }
                        log.info("Order handling completed for BigCommerce. [orderId:" + order.getId() + ",status:" + order.getStatus() + "]");
                    }
                }
                latestOrderInfo.setBcOrderLastModifiedDate(ordersList.get(0).getModifiedDate());
            }
        }catch (Exception e){
            log.error("Bigcommerce listener failed.", e);
        }
    }

    private void updateInventoriesByBigCommerce(String orderId, Integer changeType){
        log.info("Update inventories after a change in bigcommerce.[orderId:" + orderId + "]");
        try {
            List<BCOrderProduct> productList = bigCommerceAPIService.getOrderProducts(orderId);
            if(productList != null){
                for(BCOrderProduct product:productList){
                    int quantity = product.getQuantity() * changeType;
                    vendHQAPIService.updateProductQuantity(product.getSku(), quantity, false);
                    bigCommerceFSAPIService.updateProductQuantity(product.getSku(), quantity, false);
                }
            }
        }catch (Exception e){
            log.error("Failed to get product list for orders in bigcommerce.");
        }
    }


    public void listenBigCommerceFSOrders(){
        log.info("Started to check bigcommerce fs orders.");
        try {
            List<BCOrder> ordersList = bigCommerceFSAPIService.getOrders();
            if(ordersList != null){
                for(BCOrder order :ordersList){
                    if(order.getModifiedDate().after(latestOrderInfo.getBcFsOrderLastModifiedDate())){
                        log.info("An order event is captured for BigCommerceFS. [orderId:" + order.getId() + ",status:" + order.getStatus() + "]");
                        BaseOrder baseOrder = new BaseOrder("BigCommerceFS", order.getId(), order.getTotalIncludingTax(), order.getModifiedDate(), order.getStatus());
                        storeBean.getOrderStatusChanges().add(0,baseOrder);
                        if(order.getStatus().equals(BCOrderStatuses.AWAITING_FULFILLMENT)){
                            updateInventoriesByBigCommerceFS(order.getId(), InventoryChange.DECREASE);
                        } else if (order.getStatus().equals(BCOrderStatuses.CANCELLED) ||
                                order.getStatus().equals(BCOrderStatuses.DECLINED) ||
                                order.getStatus().equals(BCOrderStatuses.REFUNDED)){
                            updateInventoriesByBigCommerceFS(order.getId(), InventoryChange.INCREASE);
                        }
                        log.info("Order handling completed for BigCommerceFS. [orderId:" + order.getId() + ",status:" + order.getStatus() + "]");
                    }
                }
                latestOrderInfo.setBcFsOrderLastModifiedDate(ordersList.get(0).getModifiedDate());
            }
        }catch (Exception e){
            log.error("BigcommerceFS listener failed.", e);
        }
    }

    private void updateInventoriesByBigCommerceFS(String orderId, Integer changeType){
        log.info("Update inventories after a change in bigcommerce fs.[orderId:" + orderId + "]");
        try {
            List<BCOrderProduct> productList = bigCommerceFSAPIService.getOrderProducts(orderId);
            if(productList != null){
                for(BCOrderProduct product:productList){
                    int quantity = product.getQuantity() * changeType;
                    vendHQAPIService.updateProductQuantity(product.getSku(), quantity, false);
                    bigCommerceAPIService.updateProductQuantity(product.getSku(), quantity, false);
                }
            }
        }catch (Exception e){
            log.error("Failed to get product list for orders in bigcommerce fs.");
        }
    }


    public void listenVendHQSales(){
        log.info("Started to check vend sales.");
        try {
            List<VendHQSale> salesList = vendHQAPIService.getSales(latestOrderInfo.getVendMaxVersion());
            if(salesList != null){
                for(VendHQSale sale:salesList){
                    if(sale.getVersion() > latestOrderInfo.getVendMaxVersion()){
                        log.info("A sales event is captured for VendHQ. [saleId:" + sale.getId() + ",status:" + sale.getStatus() + "]");
                        BaseOrder baseOrder = new BaseOrder("VendHQ", sale.getId(), sale.getTotalPrice(), sale.getUpdatedAt(), sale.getStatus());

                        storeBean.getOrderStatusChanges().add(0,baseOrder);
                        if(sale.getStatus().equals(VendHQSalesStatuses.CLOSED)){
                            updateInventoriesByVendHQ(sale.getId(), sale.getProducts());
                        }
                        log.info("Order handling completed for VendHQ. [saleId:" + sale.getId() + ",status:" + sale.getStatus() + "]");
                    }
                    latestOrderInfo.setVendMaxVersion(sale.getVersion());
                }
            }
        }catch (Exception e){
            log.error("VendHQ listener failed.", e);
        }
    }

    private void updateInventoriesByVendHQ(String saleId, VendHQSalesProduct[] products){
        log.info("Update inventories after a change in vendhq.[saleId:" + saleId + "]");
        try {
            if(products != null){
                for(VendHQSalesProduct salesProduct:products){
                    // sku does not exist in sales product, get it from api
                    // TODO - change this to api 2.0
                    VendHQProduct vendProduct = vendHQAPIService.getProductById(salesProduct.getProductId());
                    String sku = vendProduct.getSku();

                    // update bigcommerce inventory
                    int quantity = salesProduct.getQuantity() * -1;
                    bigCommerceAPIService.updateProductQuantity(sku, quantity, false);
                    bigCommerceFSAPIService.updateProductQuantity(sku, quantity, false);
                }
            }
        }catch (Exception e){
            log.error("Failed to update inventory after a change in vendhq.[saleId:" + saleId + "]", e);
        }
    }
}

