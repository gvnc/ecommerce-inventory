package ecommerce.app.backend.markets.squareup.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SquareInventoryCounts {

    @Getter @Setter
    private SquareInventoryCount counts[];

    @Getter @Setter
    private String cursor;
}
