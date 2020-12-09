package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class ChangePriceResponse {

    @Getter @Setter
    @JsonProperty("catalog_object")
    private SquareItemVariation object;
}
