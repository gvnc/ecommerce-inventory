package ecommerce.app.backend.model;

import ecommerce.app.backend.amazon.products.AmazonProduct;
import ecommerce.app.backend.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.vendhq.products.VendHQProduct;
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
    private BigCommerceProduct bigCommerceFSProduct;

    @Getter @Setter
    private BigCommerceProduct bigCommerceProduct;

    @Getter @Setter
    private VendHQProduct vendHQProduct;

    @Getter @Setter
    private AmazonProduct amazonCaProduct;

    public DetailedProduct(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }
}
