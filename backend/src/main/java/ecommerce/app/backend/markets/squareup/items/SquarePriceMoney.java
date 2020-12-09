package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquarePriceMoney {
    @Getter @Setter
    private Float amount;

    @Getter @Setter
    private String currency;

}
