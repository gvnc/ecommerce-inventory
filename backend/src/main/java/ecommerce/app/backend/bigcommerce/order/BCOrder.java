package ecommerce.app.backend.bigcommerce.order;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class BCOrder {

    private String id;

    @JsonProperty("date_modified")
    private Date modifiedDate;

    private String status;

    @JsonProperty("total_inc_tax")
    private Float totalIncludingTax;

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

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Float getTotalIncludingTax() {
        return totalIncludingTax;
    }

    public void setTotalIncludingTax(Float totalIncludingTax) {
        this.totalIncludingTax = totalIncludingTax;
    }

}