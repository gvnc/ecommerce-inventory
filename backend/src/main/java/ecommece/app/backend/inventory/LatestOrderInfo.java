package ecommece.app.backend.inventory;

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

}
