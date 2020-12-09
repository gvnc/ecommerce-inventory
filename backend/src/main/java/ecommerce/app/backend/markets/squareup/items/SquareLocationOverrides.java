package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquareLocationOverrides {

    @Getter @Setter
    @JsonProperty("location_id")
    private String locationId;

    @Getter @Setter
    @JsonProperty("track_inventory")
    private Boolean trackInventory;
}
