package ecommerce.app.backend.markets.bigcommerce.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BigCommerceData {

    @Getter @Setter
    private BigCommerceProduct data [];
}
