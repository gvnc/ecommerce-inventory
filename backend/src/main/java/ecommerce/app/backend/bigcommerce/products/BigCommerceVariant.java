package ecommerce.app.backend.bigcommerce.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BigCommerceVariant {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    @JsonProperty("product_id")
    private String productId;

    @Getter @Setter
    @JsonProperty("inventory_level")
    private Integer inventoryLevel;

    @Getter @Setter
    @JsonProperty("option_values")
    private BigCommerceVariantOption optionValues[];
}
