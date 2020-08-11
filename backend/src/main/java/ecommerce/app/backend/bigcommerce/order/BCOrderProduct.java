package ecommerce.app.backend.bigcommerce.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class BCOrderProduct {
    @Getter @Setter
    @JsonProperty("product_id")
    private String productId;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private Integer quantity;
}
