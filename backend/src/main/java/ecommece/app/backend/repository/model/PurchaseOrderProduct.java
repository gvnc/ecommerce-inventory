package ecommece.app.backend.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PurchaseOrderProduct {

    @Id @Getter @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private Float price;

    @Getter @Setter
    private Integer orderedQuantity;

    @Getter @Setter
    private Integer receivedQuantity;
}