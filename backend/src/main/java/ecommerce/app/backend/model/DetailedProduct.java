package ecommerce.app.backend.model;

import ecommerce.app.backend.markets.amazon.products.AmazonProduct;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.markets.helcim.products.HelcimProduct;
import ecommerce.app.backend.markets.squareup.items.SquareProduct;
import ecommerce.app.backend.markets.vendhq.products.VendHQProduct;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DetailedProduct {

    private String sku;

    private String name;

    private Integer inventoryLevel = 0;

    private Float averageCost;

    private BigCommerceProduct bigCommerceFSProduct;

    private BigCommerceProduct bigCommerceProduct;

    private VendHQProduct vendHQProduct;

    private HelcimProduct helcimProduct;

    private AmazonProduct amazonCaProduct;

    private AmazonProduct amazonUsProduct;

    private SquareProduct squareProduct;

    public DetailedProduct(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }
}
