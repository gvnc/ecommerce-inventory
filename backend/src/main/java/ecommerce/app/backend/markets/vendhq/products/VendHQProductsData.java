package ecommerce.app.backend.markets.vendhq.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VendHQProductsData {

    @Getter @Setter
    private VendHQProduct data[];

    @Getter @Setter
    private VendHQVersion version;
}
