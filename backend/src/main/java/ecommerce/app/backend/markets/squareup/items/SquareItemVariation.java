package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SquareItemVariation {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private Integer version;

    @Getter @Setter
    @JsonProperty("present_at_all_locations")
    private Boolean presentAtAllLocations;

    @Getter @Setter
    @JsonProperty("present_at_location_ids")
    private String[] presentAtLocationIds;

    @Getter @Setter
    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @Getter @Setter
    @JsonProperty("item_variation_data")
    private SquareItemVariationData itemVariationData;

}
