package ecommerce.app.backend.markets.bigcommerce.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class BCOrder {

    @Getter @Setter
    private String id;

    @Getter @Setter
    @JsonProperty("date_modified")
    private Date modifiedDate;

    @Getter @Setter
    private String status;

    @Getter @Setter
    @JsonProperty("total_inc_tax")
    private Float totalIncludingTax;
}