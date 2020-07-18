package ecommerce.app.backend.amazon.products;

import lombok.Getter;
import lombok.Setter;

public class AmazonProduct {
    @Getter @Setter
    private String id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private Float price;

    @Getter @Setter
    private String listingId;

    @Getter @Setter
    private Integer quantity;
}
