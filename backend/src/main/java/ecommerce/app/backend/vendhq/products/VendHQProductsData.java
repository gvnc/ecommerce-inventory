package ecommerce.app.backend.vendhq.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VendHQProductsData {

    private VendHQProduct data[];

    private VendHQVersion version;

    public VendHQProduct[] getData() {
        return data;
    }

    public void setData(VendHQProduct[] data) {
        this.data = data;
    }

    public VendHQVersion getVersion() {
        return version;
    }

    public void setVersion(VendHQVersion version) {
        this.version = version;
    }
}
