package ecommece.app.backend.bigcommerce.order;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BCOrderProduct {
    @JsonProperty("product_id")
    private String productId;

    private String sku;
    private Integer quantity;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
