package ecommerce.app.backend.markets.squareup.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class ChangePhysicalCount {

    @Getter @Setter
    @JsonProperty("catalog_object_id")
    private String catalogObjectId;

    @Getter @Setter
    private String state = "IN_STOCK";

    @Getter @Setter
    private String locationId;

    @Getter @Setter
    private String quantity;

    @Getter @Setter
    @JsonProperty("occurred_at")
    private String occurredAt;
}
