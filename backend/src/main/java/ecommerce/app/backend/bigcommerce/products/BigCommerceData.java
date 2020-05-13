package ecommerce.app.backend.bigcommerce.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BigCommerceData {

    private BigCommerceProduct data [];

    public BigCommerceProduct[] getData() {
        return data;
    }

    public void setData(BigCommerceProduct[] data) {
        this.data = data;
    }
}
