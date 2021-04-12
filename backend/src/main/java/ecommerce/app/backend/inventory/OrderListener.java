package ecommerce.app.backend.inventory;

import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.amazonservices.mws.orders._2013_09_01.model.OrderItem;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.AmazonCaService;
import ecommerce.app.backend.markets.amazon.products.AmazonProduct;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.bigcommerce.order.BCOrder;
import ecommerce.app.backend.markets.bigcommerce.order.BCOrderProduct;
import ecommerce.app.backend.markets.bigcommerce.order.BCOrderStatuses;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.markets.squareup.SquareAPIService;
import ecommerce.app.backend.markets.squareup.items.SquareItemVariation;
import ecommerce.app.backend.markets.squareup.items.SquareProduct;
import ecommerce.app.backend.markets.squareup.orders.SquareLineItem;
import ecommerce.app.backend.markets.squareup.orders.SquareOrder;
import ecommerce.app.backend.markets.squareup.orders.SquareOrders;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.markets.vendhq.products.VendHQProduct;
import ecommerce.app.backend.markets.vendhq.sales.VendHQSale;
import ecommerce.app.backend.markets.vendhq.sales.VendHQSalesProduct;
import ecommerce.app.backend.markets.vendhq.sales.VendHQSalesStatuses;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.repository.model.BaseOrder;
import ecommerce.app.backend.service.OrderService;
import ecommerce.app.backend.service.constants.OrderTypeConstants;
import ecommerce.app.backend.util.Utils;
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
    private SquareAPIService squareAPIService;

    @Autowired
    private StoreBean storeBean;

    @Autowired
    private OrderService orderService;

    @Value("${order.listener.enabled}")
    private Boolean orderListenerEnabled;

    private String orderListenerDataFile;

    public OrderListener(@Value("${order.listener.data.file}")String orderListenerDataFile) {
        this.orderListenerDataFile = orderListenerDataFile;
        this.latestOrderInfo = OrderListenerUtil.getLatestOrderInfo(orderListenerDataFile);
    }

    @Scheduled(fixedDelayString = "${order.listener.delay}")
    public void listenInventoryChanges(){

        if(orderListenerEnabled == true && storeBean.getOrderListenerAllowed() == true){
            log.info("Order listener started to run.");
            listenBigCommerceOrders();
            listenVendHQSales();
            listenBigCommerceFSOrders();
            listenAmazonCAOrders();
            //listenSquareupOrders(); // remove comment out to enable vendhq
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
                        if(order.getStatus().equals(BCOrderStatuses.AWAITING_FULFILLMENT)){ // new sales
                            BaseOrder baseOrder = orderService.saveOrder("BigCommerce", order.getId(), order.getTotalIncludingTax(), order.getModifiedDate(), order.getStatus());
                            updateInventoriesByBigCommerce(order.getId(), baseOrder, InventoryChange.DECREASE);
                        } else if (order.getStatus().equals(BCOrderStatuses.CANCELLED) || // return to store
                                order.getStatus().equals(BCOrderStatuses.DECLINED) ||
                                order.getStatus().equals(BCOrderStatuses.REFUNDED)){
                            BaseOrder baseOrder = orderService.saveOrder("BigCommerce", order.getId(), order.getTotalIncludingTax(), order.getModifiedDate(), order.getStatus(), OrderTypeConstants.REFUND);
                            updateInventoriesByBigCommerce(order.getId(), baseOrder, InventoryChange.INCREASE);
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

    private void updateInventoriesByBigCommerce(String orderId, BaseOrder baseOrder, Integer changeType){
        log.info("Update inventories after a change in bigcommerce.[orderId:" + orderId + "]");
        try {
            List<BCOrderProduct> productList = bigCommerceAPIService.getOrderProducts(orderId);
            if(productList != null){
                for(BCOrderProduct product:productList){
                    if(baseOrder != null){
                        orderService.saveOrderItem(product.getSku(), product.getName(), product.getQuantity(), baseOrder);
                    }
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
                    squareAPIService.updateProductQuantity(detailedProduct.getSquareProduct(), product.getSku(), quantity, false);
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
                        if(order.getStatus().equals(BCOrderStatuses.AWAITING_FULFILLMENT)){ // new sale
                            BaseOrder baseOrder = orderService.saveOrder("BigCommerceFS", order.getId(), order.getTotalIncludingTax(), order.getModifiedDate(), order.getStatus());
                            updateInventoriesByBigCommerceFS(order.getId(), baseOrder, InventoryChange.DECREASE);
                        } else if (order.getStatus().equals(BCOrderStatuses.CANCELLED) || // returned to store
                                order.getStatus().equals(BCOrderStatuses.DECLINED) ||
                                order.getStatus().equals(BCOrderStatuses.REFUNDED)){
                            BaseOrder baseOrder = orderService.saveOrder("BigCommerceFS", order.getId(), order.getTotalIncludingTax(), order.getModifiedDate(), order.getStatus(), OrderTypeConstants.REFUND);
                            updateInventoriesByBigCommerceFS(order.getId(), baseOrder, InventoryChange.INCREASE);
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

    private void updateInventoriesByBigCommerceFS(String orderId, BaseOrder baseOrder, Integer changeType){
        log.info("Update inventories after a change in bigcommerce fs.[orderId:" + orderId + "]");
        try {
            List<BCOrderProduct> productList = bigCommerceFSAPIService.getOrderProducts(orderId);
            if(productList != null){
                for(BCOrderProduct product:productList){
                    if(baseOrder != null){
                        orderService.saveOrderItem(product.getSku(), product.getName(), product.getQuantity(), baseOrder);
                    }
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
                    squareAPIService.updateProductQuantity(detailedProduct.getSquareProduct(), product.getSku(), quantity, false);
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
                        if(sale.getStatus().equals(VendHQSalesStatuses.CLOSED)){
                            BaseOrder baseOrder = orderService.saveOrder("VendHQ", sale.getId(), sale.getTotalPrice(), sale.getUpdatedAt(), sale.getStatus());
                            updateInventoriesByVendHQ(sale.getId(), baseOrder, sale.getProducts());
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

    private void updateInventoriesByVendHQ(String saleId, BaseOrder baseOrder, VendHQSalesProduct[] products){
        log.info("Update inventories after a change in vendhq.[saleId:" + saleId + "]");
        try {
            if(products != null){
                for(VendHQSalesProduct salesProduct:products){
                    // sku does not exist in sales product, get it from api
                    // TODO - change this to api 2.0
                    VendHQProduct vendProduct = vendHQAPIService.getProductById(salesProduct.getProductId());
                    String sku = vendProduct.getSku();

                    if(baseOrder != null){
                        orderService.saveOrderItem(vendProduct.getSku(), vendProduct.getName(), salesProduct.getQuantity(), baseOrder);
                    }

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
                    squareAPIService.updateProductQuantity(detailedProduct.getSquareProduct(), sku, quantity, false);
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

                    if(amazonOrder.getOrderStatus().equals("Unshipped")) {
                        BaseOrder baseOrder = orderService.saveOrder("AmazonCA", amazonOrder.getAmazonOrderId(), orderTotal,
                                amazonOrder.getLastUpdateDate().toGregorianCalendar().getTime(), amazonOrder.getOrderStatus());
                        updateInventoriesByAmazonCA(amazonOrder.getAmazonOrderId(), baseOrder);
                    }
                }
            }
        }catch (Exception e){
            log.error("AmazonCA listener failed.", e);
        }
    }

    private void updateInventoriesByAmazonCA(String amazonOrderId, BaseOrder baseOrder){
        log.info("Update inventory after an order received in amazon ca .[OrderId:" + amazonOrderId + "]");
        try {
            List<OrderItem> orderItemList = amazonCaService.getOrderItems(amazonOrderId);
            if(orderItemList != null){
                for(OrderItem orderItem: orderItemList){
                    if(baseOrder != null){
                        orderService.saveOrderItem(orderItem.getSellerSKU(), orderItem.getTitle(), orderItem.getQuantityOrdered(), baseOrder);
                    }
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
                    squareAPIService.updateProductQuantity(detailedProduct.getSquareProduct(), sku, quantity, false);
                }
            }
        }catch (Exception e){
            log.error("Failed to update inventory after an order received in amazon ca .[OrderId:" + amazonOrderId + "]", e);
        }
    }

    public void listenSquareupOrders(){
        log.info("Started to check squareup sales.");
        try {
            // first get orders
            SquareOrders orders = squareAPIService.getOrders(latestOrderInfo.getSquareLastModifiedDate());

            // and second set last update date
            latestOrderInfo.setSquareLastModifiedDate(new Date());

            if(orders != null && orders.getOrders() != null){
                for(SquareOrder squareOrder:orders.getOrders()){
                    log.info("An order is captured for SquareUp. [orderId:" + squareOrder.getId() + ",status:" + squareOrder.getState() + "]");

                    Float totalMoney = Utils.centsToDollar(squareOrder.getTotalMoney().getAmount());
                    BaseOrder baseOrder = orderService.saveOrder("SquareUp", squareOrder.getId(), totalMoney, squareOrder.getUpdatedAt(), squareOrder.getState());
                    updateInventoriesBySquareUp(squareOrder.getId(), baseOrder, squareOrder.getLineItems());

                    log.info("Order handling completed for SquareUp. [orderId:" + squareOrder.getId() + ",status:" + squareOrder.getState() + "]");

                }
            }
        }catch (Exception e){
            log.error("SquareUp listener failed.", e);
        }
    }

    private void updateInventoriesBySquareUp(String orderId, BaseOrder baseOrder, SquareLineItem[] items){
        log.info("Update inventories after a change in squareup.[orderId:" + orderId + "]");
        try {
            if(items != null){
                for(SquareLineItem item:items){
                    SquareItemVariation itemVariation = squareAPIService.getProductById(item.getCatalogObjectId());
                    if(baseOrder != null){
                        orderService.saveOrderItem(itemVariation.getItemVariationData().getSku(), itemVariation.getItemVariationData().getName(),
                                Integer.parseInt(item.getQuantity()), baseOrder);
                    }
                    String sku = itemVariation.getItemVariationData().getSku();

                    int quantity = Integer.parseInt(item.getQuantity()) * -1;

                    // set overall quantity
                    DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(sku);
                    setOverallQuantity(detailedProduct, quantity);

                    // update inventory level in memory for product in base market
                    SquareProduct squareProduct = detailedProduct.getSquareProduct();
                    if(squareProduct != null){
                        if(squareProduct.getInventory() != null) {
                            int currentQuantity = squareProduct.getInventory();
                            int newQuantity = currentQuantity + quantity;
                            if (newQuantity < 0) {
                                log.warn("There is no enough inventory in the squareup store for sku " +sku + ". [currentQuantity:" + currentQuantity + ", demanded:" + quantity);
                                log.warn("Set inventory to 0 for squareup sku " + sku);
                                newQuantity = 0;
                            }
                            squareProduct.setInventory(newQuantity);
                            BaseProduct baseProduct = storeBean.getProductsMap().get(sku);
                            if(baseProduct != null){
                                baseProduct.setSquareInventory(newQuantity);
                            }
                        }
                    }

                    // set quantity for other marketplaces
                    vendHQAPIService.updateProductQuantity(detailedProduct.getVendHQProduct(), sku, quantity, false);
                    bigCommerceAPIService.updateProductQuantity(detailedProduct.getBigCommerceProduct(), sku, quantity, false);
                    bigCommerceFSAPIService.updateProductQuantity(detailedProduct.getBigCommerceFSProduct(), sku, quantity, false);
                    amazonCaService.updateInventory(sku, quantity, false);
                }
            }
        }catch (Exception e){
            log.error("Failed to update inventory after a change in squareup.[orderId:" + orderId + "]", e);
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

