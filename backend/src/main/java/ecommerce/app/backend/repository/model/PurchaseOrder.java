package ecommerce.app.backend.repository.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ecommerce.app.backend.util.Utils;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class PurchaseOrder {

    @Id @Getter @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    private String status;

    @Getter @Setter
    private String createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Getter @Setter
    private Date createDate;

    @Getter @Setter
    private String supplier;

    @Getter @Setter
    private Float discount = 0F;

    @Getter @Setter
    private Float shipping = 0F;

    @Getter @Setter
    private Float salesTax = 0F;

    @Getter @Setter
    private Float orderTotal = 0F;
}
