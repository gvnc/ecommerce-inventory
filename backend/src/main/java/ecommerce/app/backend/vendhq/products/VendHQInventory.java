package ecommerce.app.backend.vendhq.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class VendHQInventory {

    @Getter @Setter
    @JsonProperty("product_id")
    private String productId;

    @Getter @Setter
    @JsonProperty("inventory_level")
    private Integer inventoryLevel;

    @Getter @Setter
    private Integer count;

    @Getter @Setter
    @JsonProperty("outlet_id")
    private String outletId;
}