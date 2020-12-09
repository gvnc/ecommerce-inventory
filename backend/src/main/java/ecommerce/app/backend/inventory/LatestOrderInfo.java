package ecommerce.app.backend.inventory;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class LatestOrderInfo {

    @Getter @Setter
    private Long vendMaxVersion;

    @Getter @Setter
    private Date bcOrderLastModifiedDate;

    @Getter @Setter
    private Date bcFsOrderLastModifiedDate;

    @Getter @Setter
    private Date amazonCaLastUpdate;

    @Getter @Setter
    private Date squareLastModifiedDate;
}
