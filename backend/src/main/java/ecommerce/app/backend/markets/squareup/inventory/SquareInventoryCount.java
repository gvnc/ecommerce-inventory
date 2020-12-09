package ecommerce.app.backend.markets.squareup.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquareInventoryCount {

    @Getter @Setter
    @JsonProperty("catalog_object_id")
    private String catalogObjectId;

    @Getter @Setter
    private Integer quantity;
}
