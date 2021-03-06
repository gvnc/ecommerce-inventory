package ecommerce.app.backend.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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
    private String supplierCode;

    @Getter @Setter
    private Float costPrice;

    @Getter @Setter
    private Integer dutyRate;

    @Getter @Setter
    private Integer orderedQuantity;

    @Getter @Setter
    private Integer remainingQuantity;

    @Getter @Setter
    private Integer receivedQuantity;

    @Getter @Setter
    @OneToOne
    @JoinColumn(name = "purchaseOrderId", referencedColumnName = "id")
    private PurchaseOrder purchaseOrder;
}