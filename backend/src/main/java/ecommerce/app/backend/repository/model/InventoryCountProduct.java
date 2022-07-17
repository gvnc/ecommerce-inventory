package ecommerce.app.backend.repository.model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class InventoryCountProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String sku;

    private String name;

    private Integer vendhqQuantity;

    private Integer bigcommerceQuantity;

    private Integer bigcommerceFSQuantity;

    private Integer amazonCAQuantity;

    private Integer helcimQuantity;

    private Integer count = 0;

    private Boolean counted = false;

    private Boolean matched = false;

    private Integer inventoryCountId;
}
