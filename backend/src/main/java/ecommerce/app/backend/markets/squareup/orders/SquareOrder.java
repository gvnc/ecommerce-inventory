package ecommerce.app.backend.markets.squareup.orders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquareOrder {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String state;

    @Getter @Setter
    @JsonProperty("updated_at")
    private String updatedAt;

    @Getter @Setter
    @JsonProperty("line_items")
    private SquareLineItem[] lineItems;
}
