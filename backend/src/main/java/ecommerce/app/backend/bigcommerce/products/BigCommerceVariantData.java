package ecommerce.app.backend.bigcommerce.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BigCommerceVariantData {

    @Getter @Setter
    private BigCommerceVariant data [];
}
