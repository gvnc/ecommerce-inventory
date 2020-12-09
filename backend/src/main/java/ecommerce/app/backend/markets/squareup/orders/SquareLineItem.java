package ecommerce.app.backend.markets.squareup.orders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquareLineItem {

    @Getter @Setter
    @JsonProperty("catalog_object_id")
    private String catalogObjectId;

    @Getter @Setter
    private String quantity;
}
