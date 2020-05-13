package ecommerce.app.backend.repository.model;

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
    private Date submitDate;

    @Getter @Setter
    private String supplier;

    @Getter @Setter
    private Float discount;

    @Getter @Setter
    private Float shipping;

    @Getter @Setter
    private Float salesTax;

    @Getter @Setter
    private Float totalPrice;
}
