package ecommerce.app.backend.markets.vendhq.products;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VendHQResponseProduct {

    private VendHQProduct product;

    public VendHQProduct getProduct() {
        return product;
    }

    public void setProduct(VendHQProduct product) {
        this.product = product;
    }
}
