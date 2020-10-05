package ecommerce.app.backend.markets.vendhq.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VendHQProducts {

    private VendHQProduct products[];

    public VendHQProduct[] getProducts() {
        return products;
    }

    public void setProducts(VendHQProduct[] products) {
        this.products = products;
    }
}
