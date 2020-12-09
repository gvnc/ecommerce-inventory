package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquareItemData {

    @Getter @Setter
    private String name;

    @Getter @Setter
    @JsonProperty("product_type")
    private String productType;

    @Getter @Setter
    private SquareItemVariation [] variations;
}