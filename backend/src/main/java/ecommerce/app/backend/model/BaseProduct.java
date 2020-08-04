package ecommerce.app.backend.model;

import lombok.Getter;
import lombok.Setter;

public class BaseProduct {

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private Float bigCommercePrice;

    @Getter @Setter
    private Float bigCommerceFSPrice;

    @Getter @Setter
    private Float vendHQPrice;

    @Getter @Setter
    private Float amazonCAPrice;

    public BaseProduct(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }
}
