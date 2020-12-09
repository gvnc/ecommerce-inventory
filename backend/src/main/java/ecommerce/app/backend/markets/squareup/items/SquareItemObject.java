package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquareItemObject {

    @Getter @Setter
    @JsonProperty("item_data")
    private SquareItemData itemData;
}
