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
    private String name;

    @Getter @Setter
    private Integer vendhqQuantity;

    @Getter @Setter
    private Integer bigcommerceQuantity;

    @Getter @Setter
    private Integer bigcommerceFSQuantity;

    @Getter @Setter
    private Integer amazonCAQuantity;

    @Getter @Setter
    private Integer count = 0;

    @Getter @Setter
    private Boolean counted = false;

    @Getter @Setter
    private Boolean matched = false;

    @Getter @Setter
    private Integer inventoryCountId;
}
