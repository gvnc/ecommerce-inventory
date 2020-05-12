package ecommece.app.backend.vendhq.products;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VendHQProduct {

    private String id;
    private String name;
    private String sku;
    private Boolean active;

    @JsonProperty("price_including_tax")
    private Float price;

    @JsonProperty("supply_price")
    private Float supplyPrice;

    // made up this property to fix deserialization issue
    @JsonProperty("product_inventory")
    private VendHQInventory inventory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getSupplyPrice() {
        return supplyPrice;
    }

    public void setSupplyPrice(Float supplyPrice) {
        this.supplyPrice = supplyPrice;
    }

    public VendHQInventory getInventory() {
        return inventory;
    }

    public void setInventory(VendHQInventory inventory) {
        this.inventory = inventory;
    }
}
