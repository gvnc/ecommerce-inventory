package ecommerce.app.backend.model;

import ecommerce.app.backend.controller.OperationConstants;
import lombok.Getter;
import lombok.Setter;

public class InventoryUpdateResult {

    @Getter @Setter
    private String bigCommerceInventoryUpdate = OperationConstants.NA;

    @Getter @Setter
    private String bigCommerceFSInventoryUpdate = OperationConstants.NA;

    @Getter @Setter
    private String vendhqInventoryUpdate = OperationConstants.NA;

    @Getter @Setter
    private String squareInventoryUpdate = OperationConstants.NA;

    @Getter @Setter
    private String amazonUsInventoryUpdate = OperationConstants.NA;

    @Getter @Setter
    private String amazonCaInventoryUpdate = OperationConstants.NA;

    @Getter @Setter
    private String finalResult = OperationConstants.NA;
}
