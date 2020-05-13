package ecommerce.app.backend.vendhq.sales;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class VendHQSale {

    private String id;

    private String status;

    @JsonProperty("total_price_incl")
    private Float totalPrice;

    private Long version;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonProperty("line_items")
    private VendHQSalesProduct products [];

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public VendHQSalesProduct[] getProducts() {
        return products;
    }

    public void setProducts(VendHQSalesProduct[] products) {
        this.products = products;
    }
}
