package ecommerce.app.backend.inventory;

import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.amazonservices.mws.orders._2013_09_01.model.OrderItem;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.amazon.AmazonCaService;
import ecommerce.app.backend.amazon.products.AmazonProduct;
import ecommerce.app.backend.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.bigcommerce.order.BCOrder;
import ecommerce.app.backend.bigcommerce.order.BCOrderProduct;
import ecommerce.app.backend.bigcommerce.order.BCOrderStatuses;
import ecommerce.app.backend.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.model.BaseOrder;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
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

import java.util.Date;
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
    private AmazonCaService amazonCaService;

    @Autowired
    private StoreBean storeBean;

    @Value("${order.listener.enabled}")
    private Boolean orderListenerEnabled;

    private String orderListenerDataFile;

    public OrderListener(@Value("${order.listener.data.file}")String orderListenerDataFile) {
        this.orderListenerDataFile = orderListenerDataFile;
        this.latestOrderInfo = OrderListenerUtil.getLatestOrderInfo(orderListenerDataFile);
    }

    @Scheduled(fixedDelayString = "${order.listener.delay}")
    public void listenInventoryChanges(){

        if(orderListenerEnabled == true){
            // TODO if sync is in progress do nothing !!
            log.info("Order listener started to run.");
            listenBigCommerceOrders();
            listenVendHQSales();
            listenBigCommerceFSOrders();
            listenAmazonCAOrders();
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

                    // set overall quantity
                    DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(product.getSku());
                    setOverallQuantity(detailedProduct, quantity);

                    // update inventory level in memory for product in base market
                    BigCommerceProduct bigCommerceProduct = detailedProduct.getBigCommerceProduct();
                    if(bigCommerceProduct != null){
                        int currentQuantity = bigCommerceProduct.getInventoryLevel();
                        int newQuantity = currentQuantity + quantity;
                        if (newQuantity < 0) {
                            log.warn("There is no enough inventory in the bigcommerce store for sku " + product.getSku() + ". [currentQuantity:" + currentQuantity + ", demanded:" + quantity);
                            log.warn("Set inventory to 0 for bigcommerce sku " + product.getSku());
                            newQuantity = 0;
                        }
                        bigCommerceProduct.setInventoryLevel(newQuantity);
                        BaseProduct baseProduct = storeBean.getProductsMap().get(product.getSku());
                        if(baseProduct != null){
                            baseProduct.setBigCommerceInventory(newQuantity);
                        }
                    }

                    // set quantity for other market places
                    vendHQAPIService.updateProductQuantity(detailedProduct.getVendHQProduct(), product.getSku(), quantity, false);
                    bigCommerceFSAPIService.updateProductQuantity(detailedProduct.getBigCommerceFSProduct(), product.getSku(), quantity, false);
                    amazonCaService.updateInventory(product.getSku(), quantity, false);
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

                    // set overall quantity
                    DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(product.getSku());
                    setOverallQuantity(detailedProduct, quantity);

                    // update inventory level in memory for product in base market
                    BigCommerceProduct bigCommerceProduct = detailedProduct.getBigCommerceFSProduct();
                    if(bigCommerceProduct != null){
                        int currentQuantity = bigCommerceProduct.getInventoryLevel();
                        int newQuantity = currentQuantity + quantity;
                        if (newQuantity < 0) {
                            log.warn("There is no enough inventory in the bigcommerce-fs store for sku " + product.getSku() + ". [currentQuantity:" + currentQuantity + ", demanded:" + quantity);
                            log.warn("Set inventory to 0 for bigcommerce-fs sku " + product.getSku());
                            newQuantity = 0;
                        }
                        bigCommerceProduct.setInventoryLevel(newQuantity);
                        BaseProduct baseProduct = storeBean.getProductsMap().get(product.getSku());
                        if(baseProduct != null){
                            baseProduct.setBigCommerceFSInventory(newQuantity);
                        }
                    }

                    // set quantity for other market places
                    vendHQAPIService.updateProductQuantity(detailedProduct.getVendHQProduct(), product.getSku(), quantity, false);
                    bigCommerceAPIService.updateProductQuantity(detailedProduct.getBigCommerceProduct(), product.getSku(), quantity, false);
                    amazonCaService.updateInventory(product.getSku(), quantity, false);
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
                    int quantity = salesProduct.getQuantity() * -1;

                    // set overall quantity
                    DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(sku);
                    setOverallQuantity(detailedProduct, quantity);

                    // update inventory level in memory for product in base market
                    VendHQProduct vendHQProduct = detailedProduct.getVendHQProduct();
                    if(vendHQProduct != null){
                        if(vendHQProduct.getInventory() != null) {
                            int currentQuantity = vendHQProduct.getInventory().getInventoryLevel();
                            int newQuantity = currentQuantity + quantity;
                            if (newQuantity < 0) {
                                log.warn("There is no enough inventory in the vendhq store for sku " +sku + ". [currentQuantity:" + currentQuantity + ", demanded:" + quantity);
                                log.warn("Set inventory to 0 for vendhq sku " + sku);
                                newQuantity = 0;
                            }
                            vendHQProduct.getInventory().setInventoryLevel(newQuantity);
                            vendHQProduct.getInventory().setCount(newQuantity);
                            BaseProduct baseProduct = storeBean.getProductsMap().get(sku);
                            if(baseProduct != null){
                                baseProduct.setVendHQInventory(newQuantity);
                            }
                        }
                    }

                    // set quantity for other marketplaces
                    bigCommerceAPIService.updateProductQuantity(detailedProduct.getBigCommerceProduct(), sku, quantity, false);
                    bigCommerceFSAPIService.updateProductQuantity(detailedProduct.getBigCommerceFSProduct(), sku, quantity, false);
                    amazonCaService.updateInventory(sku, quantity, false);
                }
            }
        }catch (Exception e){
            log.error("Failed to update inventory after a change in vendhq.[saleId:" + saleId + "]", e);
        }
    }

    public void listenAmazonCAOrders(){
        log.info("Started to check amazon ca orders.");
        try {
            // get orders first
            List<Order> orderList = amazonCaService.getOrders(latestOrderInfo.getAmazonCaLastUpdate());

            // and second set last update date
            latestOrderInfo.setAmazonCaLastUpdate(new Date());

            // process the orders
            if(orderList != null){
                for(Order amazonOrder:orderList){
                    log.info("An order is captured for AmazonCA. [orderId:" + amazonOrder.getAmazonOrderId() + ",status:" + amazonOrder.getOrderStatus() + "]");
                    String orderTotal = "0";
                    if(amazonOrder.getOrderTotal() != null){
                        orderTotal = amazonOrder.getOrderTotal().getAmount();
                    }

                    BaseOrder baseOrder = new BaseOrder("AmazonCA", amazonOrder.getAmazonOrderId(), orderTotal,
                            amazonOrder.getLastUpdateDate().toGregorianCalendar().getTime(), amazonOrder.getOrderStatus());

                    storeBean.getOrderStatusChanges().add(0,baseOrder);
                    if(amazonOrder.getOrderStatus().equals("Unshipped")) {
                        updateInventoriesByAmazonCA(amazonOrder.getAmazonOrderId());
                    }
                }
            }
        }catch (Exception e){
            log.error("AmazonCA listener failed.", e);
        }
    }

    private void updateInventoriesByAmazonCA(String amazonOrderId){
        log.info("Update inventory after an order received in amazon ca .[OrderId:" + amazonOrderId + "]");
        try {
            List<OrderItem> orderItemList = amazonCaService.getOrderItems(amazonOrderId);
            if(orderItemList != null){
                for(OrderItem orderItem: orderItemList){
                    String sku = orderItem.getSellerSKU();
                    Integer quantity = orderItem.getQuantityOrdered();
                    quantity = quantity * -1;

                    // set overall quantity
                    DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(sku);
                    setOverallQuantity(detailedProduct, quantity);

                    // update inventory level in memory for product in base market
                    AmazonProduct amazonProduct = detailedProduct.getAmazonCaProduct();
                    if(amazonProduct != null){
                        int currentQuantity = amazonProduct.getQuantity();
                        int newQuantity = currentQuantity + quantity;
                        if (newQuantity < 0) {
                            log.warn("There is no enough inventory in the amazon-ca store for sku " + sku + ". [currentQuantity:" + currentQuantity + ", demanded:" + quantity);
                            log.warn("Set inventory to 0 for amazon-ca sku " + sku);
                            newQuantity = 0;
                        }
                        amazonProduct.setQuantity(newQuantity);
                        BaseProduct baseProduct = storeBean.getProductsMap().get(sku);
                        if(baseProduct != null){
                            baseProduct.setAmazonCAInventory(newQuantity);
                        }
                    }

                    // update quantity for other market places
                    bigCommerceAPIService.updateProductQuantity(detailedProduct.getBigCommerceProduct(), sku, quantity, false);
                    bigCommerceFSAPIService.updateProductQuantity(detailedProduct.getBigCommerceFSProduct(), sku, quantity, false);
                    vendHQAPIService.updateProductQuantity(detailedProduct.getVendHQProduct(), sku, quantity, false);
                }
            }
        }catch (Exception e){
            log.error("Failed to update inventory after an order received in amazon ca .[OrderId:" + amazonOrderId + "]", e);
        }
    }

    private void setOverallQuantity(DetailedProduct detailedProduct, Integer quantity){
        try {
            int newQuantity = detailedProduct.getInventoryLevel() + quantity;
            if (newQuantity < 0) {
                log.warn("There is no enough inventory in overall for sku " + detailedProduct.getSku() +
                        ". [currentQuantity:" + detailedProduct.getInventoryLevel() + ", demanded:" + quantity + "]");
                newQuantity = 0;
            }
            detailedProduct.setInventoryLevel(newQuantity);
        } catch (Exception e){
            log.error("Error in setOverallQuantity.", e);
        }
    }
}

