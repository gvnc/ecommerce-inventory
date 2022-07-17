package ecommerce.app.backend.model;

import ecommerce.app.backend.controller.OperationConstants;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommitPriceResult {

    private String bigCommercePriceChange = OperationConstants.NA;

    private String bigCommerceFSPriceChange = OperationConstants.NA;

    private String vendhqPriceChange = OperationConstants.NA;

    private String squarePriceChange = OperationConstants.NA;

    private String amazonUsPriceChange = OperationConstants.NA;

    private String amazonCaPriceChange = OperationConstants.NA;

    private String helcimPriceChange = OperationConstants.NA;

    private String finalResult = OperationConstants.NA;
}
