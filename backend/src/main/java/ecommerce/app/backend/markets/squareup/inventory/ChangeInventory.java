package ecommerce.app.backend.markets.squareup.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class ChangeInventory {

    @Getter @Setter
    @JsonProperty("physical_count")
    private ChangePhysicalCount physicalCount;

    @Getter @Setter
    private String type = "PHYSICAL_COUNT";
}
