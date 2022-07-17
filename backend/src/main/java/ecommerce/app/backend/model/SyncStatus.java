package ecommerce.app.backend.model;

import ecommerce.app.backend.service.constants.SyncConstants;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SyncStatus {

    private String overallSyncStatus = SyncConstants.SYNC_NA;

    private String bigCommerceSyncStatus = SyncConstants.SYNC_NA;
    private String bigCommerceLastUpdate = "";

    private String bigCommerceFSSyncStatus = SyncConstants.SYNC_NA;
    private String bigCommerceFSLastUpdate = "";

    private String vendHQSyncStatus = SyncConstants.SYNC_NA;
    private String vendHQLastUpdate = "";

    private String amazonUsStatus = SyncConstants.SYNC_NA;
    private String amazonUsLastUpdate = "";

    private String amazonCaStatus = SyncConstants.SYNC_NA;
    private String amazonCaLastUpdate = "";

    private String squareupSyncStatus = SyncConstants.SYNC_NA;
    private String squareupLastUpdate = "";

    private String helcimSyncStatus = SyncConstants.SYNC_NA;
    private String helcimLastUpdate = "";

    private String syncMaster;

    public SyncStatus setMaster(String syncMaster){
        this.syncMaster = syncMaster;
        return this;
    }
}
