package ecommerce.app.backend.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
public class PurchaseOrderAttachment {

    @Id @Getter @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    private String filename;

    @Lob @Getter @Setter
    private byte[] data;

    @Getter @Setter
    @OneToOne
    @JoinColumn(name = "purchaseOrderId", referencedColumnName = "id")
    private PurchaseOrder purchaseOrder;

}
