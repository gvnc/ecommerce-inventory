package ecommerce.app.backend.service;

import ecommerce.app.backend.repository.BaseOrderItemRepository;
import ecommerce.app.backend.repository.BaseOrderProductRepository;
import ecommerce.app.backend.repository.BaseOrderRepository;
import ecommerce.app.backend.repository.model.BaseOrder;
import ecommerce.app.backend.repository.model.BaseOrderItem;
import ecommerce.app.backend.repository.model.BaseOrderProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private BaseOrderRepository baseOrderRepository;

    @Autowired
    private BaseOrderItemRepository baseOrderItemRepository;

    @Autowired
    private BaseOrderProductRepository baseOrderProductRepository;

    public BaseOrder saveOrder(String marketPlace, String orderId, Float totalPrice, Date modifiedDate, String status) {
        try {
            BaseOrder baseOrder = new BaseOrder();
            baseOrder.setOrderId(orderId);
            baseOrder.setMarketPlace(marketPlace);
            baseOrder.setStatus(status);
            baseOrder.setInsertDate(modifiedDate);
            baseOrder.setTotalPrice(totalPrice);

            return baseOrderRepository.save(baseOrder);
        } catch (Exception e){
            log.error("Failed to save order to database.", e);
        }
        return null;
    }

    public BaseOrder saveOrder(String marketPlace, String orderId, String totalPrice, Date modifiedDate, String status) {
        Float totalPriceFloat = 0F;
        try{
            totalPriceFloat = Float.parseFloat(totalPrice);
        } catch (Exception e){}

        return saveOrder(marketPlace, orderId, totalPriceFloat, modifiedDate, status);
    }

    public BaseOrder saveOrder(String marketPlace, String orderId, Float totalPrice, String modifiedDate, String status) {
        // this is used by square only - format this as in the example - 2020-01-25T18:25:34-08:00
        Date date = new Date();
        return saveOrder(marketPlace, orderId, totalPrice, date, status);
    }

    public void saveOrderItem(String sku, String productName, Integer quantity, BaseOrder baseOrder){
        if(quantity > 0) {
            try {
                BaseOrderProduct product = new BaseOrderProduct();
                product.setSku(sku);
                product.setProductName(productName);
                baseOrderProductRepository.save(product);
            } catch (Exception e) {
                log.error("Can not save order product.", e);
            }

            try {
                BaseOrderItem baseOrderItem = new BaseOrderItem();
                baseOrderItem.setSku(sku);
                baseOrderItem.setQuantity(quantity);
                baseOrderItem.setBaseOrder(baseOrder);
                baseOrderItem.setInsertDate(baseOrder.getInsertDate());
                baseOrderItemRepository.save(baseOrderItem);
            } catch (Exception e) {
                log.error("Can not save order item.", e);
            }
        }
    }

    public List<BaseOrder> getOrdersFor3MonthsBack(){
        return null;
    }
}
