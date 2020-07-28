package ecommerce.app.backend.model;

import ecommerce.app.backend.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class BaseOrder {

    @Getter @Setter
    private String marketPlace;
    @Getter @Setter
    private String orderId;
    @Getter @Setter
    private Float totalPrice;
    @Getter @Setter
    private String status;
    @Getter @Setter
    private String dateModified;

    public BaseOrder(String marketPlace, String orderId, Float totalPrice, Date modifiedDate, String status) {
        this.marketPlace = marketPlace;
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.dateModified = Utils.getDateAsString(modifiedDate);
    }

    public BaseOrder(String marketPlace, String orderId, String totalPrice, Date modifiedDate, String status) {
        this.marketPlace = marketPlace;
        this.orderId = orderId;

        try{
            this.totalPrice = Float.parseFloat(totalPrice);
        } catch (Exception e){}

        this.status = status;
        this.dateModified = Utils.getDateAsString(modifiedDate);
    }
}
