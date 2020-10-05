package ecommerce.app.backend.markets.vendhq.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class VendHQProduct {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    @JsonProperty("variant_name")
    private String variantName;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private Boolean active;

    @Getter @Setter
    @JsonProperty("price_including_tax")
    private Float price;

    @Getter @Setter
    @JsonProperty("supply_price")
    private Float supplyPrice;

    // made up this property to fix deserialization issue
    @Getter @Setter
    @JsonProperty("product_inventory")
    private VendHQInventory inventory;
}
