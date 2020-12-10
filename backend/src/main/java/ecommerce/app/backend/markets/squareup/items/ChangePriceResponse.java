package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangePriceResponse {

    @Getter @Setter
    @JsonProperty("catalog_object")
    private SquareItemVariation object;
}
