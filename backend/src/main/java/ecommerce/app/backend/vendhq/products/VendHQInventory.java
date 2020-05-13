package ecommerce.app.backend.vendhq.products;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VendHQInventory {

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("inventory_level")
    private Integer inventoryLevel;

    private Integer count;

    @JsonProperty("outlet_id")
    private String outletId;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getInventoryLevel() {
        return inventoryLevel;
    }

    public void setInventoryLevel(Integer inventoryLevel) {
        this.inventoryLevel = inventoryLevel;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getOutletId() {
        return outletId;
    }

    public void setOutletId(String outletId) {
        this.outletId = outletId;
    }
}
