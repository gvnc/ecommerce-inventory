package ecommerce.app.backend.markets.bigcommerce.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BigCommerceVariantData {

    @Getter @Setter
    private BigCommerceVariant data [];
}
