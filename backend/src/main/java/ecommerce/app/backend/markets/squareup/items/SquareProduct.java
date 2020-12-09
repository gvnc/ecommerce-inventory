package ecommerce.app.backend.markets.squareup.items;

import lombok.Getter;
import lombok.Setter;

public class SquareProduct {

    @Getter @Setter
    private String variationId;

    @Getter @Setter
    private String itemId;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private Float price;

    @Getter @Setter
    private Integer inventory;
}
