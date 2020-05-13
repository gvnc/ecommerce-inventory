package ecommerce.app.backend.model;

import lombok.Getter;
import lombok.Setter;

public class BaseProduct {

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private String name;

    public BaseProduct(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }
}
