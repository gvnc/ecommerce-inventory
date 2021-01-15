package ecommerce.app.backend.model;

import ecommerce.app.backend.markets.amazon.products.AmazonProduct;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.markets.squareup.items.SquareProduct;
import ecommerce.app.backend.markets.vendhq.products.VendHQProduct;
import lombok.Getter;
import lombok.Setter;

public class DetailedProduct {

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private Integer inventoryLevel = 0;

    @Getter @Setter
    private Float averageCost;

    @Getter @Setter
    private BigCommerceProduct bigCommerceFSProduct;

    @Getter @Setter
    private BigCommerceProduct bigCommerceProduct;

    @Getter @Setter
    private VendHQProduct vendHQProduct;

    @Getter @Setter
    private AmazonProduct amazonCaProduct;

    @Getter @Setter
    private AmazonProduct amazonUsProduct;

    @Getter @Setter
    private SquareProduct squareProduct;

    public DetailedProduct(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }
}
