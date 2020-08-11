package ecommerce.app.backend.bigcommerce.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BigCommerceVariantOption {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String label;

    @Getter @Setter
    @JsonProperty("option_display_name")
    private String optionDisplayName;
}
