package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquareItemVariationData {

    @Getter @Setter
    @JsonProperty("item_id")
    private String itemId;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private Integer ordinal;

    @Getter @Setter
    @JsonProperty("pricing_type")
    private String pricingType;

    @Getter @Setter
    @JsonProperty("price_money")
    private SquarePriceMoney priceMoney;

    @Getter @Setter
    @JsonProperty("location_overrides")
    private SquareLocationOverrides[] locationOverrides;
}
