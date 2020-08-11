package ecommerce.app.backend.bigcommerce.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BigCommerceData {

    @Getter @Setter
    private BigCommerceProduct data [];
}
