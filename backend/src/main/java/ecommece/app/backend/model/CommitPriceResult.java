package ecommece.app.backend.model;

import ecommece.app.backend.controller.OperationConstants;
import lombok.Getter;
import lombok.Setter;

public class CommitPriceResult {

    @Getter @Setter
    private String bigCommercePriceChange = OperationConstants.NA;

    @Getter @Setter
    private String bigCommerceFSPriceChange = OperationConstants.NA;

    @Getter @Setter
    private String vendhqPriceChange = OperationConstants.NA;

    @Getter @Setter
    private String amazonUsPriceChange = OperationConstants.NA;

    @Getter @Setter
    private String amazonCaPriceChange = OperationConstants.NA;

    @Getter @Setter
    private String finalResult = OperationConstants.NA;
}
