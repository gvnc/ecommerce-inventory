package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class ChangePriceBody {

    @Getter @Setter
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @Getter @Setter
    private SquareItemVariation object;
}
