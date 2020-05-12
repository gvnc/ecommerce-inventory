package ecommece.app.backend.vendhq.sales;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VendHQSales {

    private VendHQSale data[];

    public VendHQSale[] getData() {
        return data;
    }

    public void setData(VendHQSale[] data) {
        this.data = data;
    }
}
