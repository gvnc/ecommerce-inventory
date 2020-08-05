package ecommerce.app.backend.repository.model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
public class InventoryCountProduct {

    @Id @Getter @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private Integer venhqQuantity;

    @Getter @Setter
    private Integer bigcommerceQuantity;

    @Getter @Setter
    private Integer bigcommerceFSQuantity;

    @Getter @Setter
    private Integer amazonCAQuantity;

    @Getter @Setter
    private Integer count;

    @Getter @Setter
    private Boolean counted;

    @Getter @Setter
    private String status; // Matched - Unmatched

    @Getter @Setter
    @OneToOne
    @JoinColumn(name = "inventoryCountId", referencedColumnName = "id")
    private InventoryCount inventoryCount;
}
