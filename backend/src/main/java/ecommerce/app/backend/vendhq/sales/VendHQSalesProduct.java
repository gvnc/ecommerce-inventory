package ecommerce.app.backend.vendhq.sales;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VendHQSalesProduct {

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("is_return")
    private Boolean isReturn;

    private Integer quantity;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Boolean getReturn() {
        return isReturn;
    }

    public void setReturn(Boolean aReturn) {
        isReturn = aReturn;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
