package ecommerce.app.backend.markets.squareup.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeInventoryBody {

    @Getter @Setter
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @Getter @Setter
    private ChangeInventory[] changes;
}
