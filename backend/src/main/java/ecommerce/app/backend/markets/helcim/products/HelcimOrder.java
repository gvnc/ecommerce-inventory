package ecommerce.app.backend.markets.helcim.products;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class HelcimOrder {
    private String orderNumber;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dateIssued;
    private Float amount;
    private String status;
    private HelcimOrderItem[] items;
}
