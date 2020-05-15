package ecommerce.app.backend.model;

import ecommerce.app.backend.repository.model.PurchaseOrder;
import ecommerce.app.backend.repository.model.PurchaseOrderProduct;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PurchaseOrderRequest {

    @Getter @Setter
    private PurchaseOrder purchaseOrder;

    @Getter @Setter
    private List<PurchaseOrderProduct> productList;
}
