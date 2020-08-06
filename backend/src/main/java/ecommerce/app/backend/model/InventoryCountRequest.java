package ecommerce.app.backend.model;

import ecommerce.app.backend.repository.model.InventoryCount;
import ecommerce.app.backend.repository.model.InventoryCountProduct;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class InventoryCountRequest {

    @Getter @Setter
    private InventoryCount inventoryCount;

    @Getter @Setter
    private List<InventoryCountProduct> productList;
}
