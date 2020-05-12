package ecommece.app.backend.vendhq.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VendHQInventoryData {

    private VendHQInventory data[];

    private VendHQVersion version;

    public VendHQInventory[] getData() {
        return data;
    }

    public void setData(VendHQInventory[] data) {
        this.data = data;
    }

    public VendHQVersion getVersion() {
        return version;
    }

    public void setVersion(VendHQVersion version) {
        this.version = version;
    }
}
