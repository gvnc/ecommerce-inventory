package ecommerce.app.backend.markets.helcim.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelcimOrderItem {

    @JsonProperty("SKU")
    private String sku;
    private String description;
    private Float quantity;
}
