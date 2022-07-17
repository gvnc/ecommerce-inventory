package ecommerce.app.backend.model;

import ecommerce.app.backend.controller.OperationConstants;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InventoryUpdateResult {

    private String bigCommerceInventoryUpdate = OperationConstants.NA;

    private String bigCommerceFSInventoryUpdate = OperationConstants.NA;

    private String vendhqInventoryUpdate = OperationConstants.NA;

    private String squareInventoryUpdate = OperationConstants.NA;

    private String amazonUsInventoryUpdate = OperationConstants.NA;

    private String amazonCaInventoryUpdate = OperationConstants.NA;

    private String helcimInventoryUpdate = OperationConstants.NA;

    private String finalResult = OperationConstants.NA;
}
