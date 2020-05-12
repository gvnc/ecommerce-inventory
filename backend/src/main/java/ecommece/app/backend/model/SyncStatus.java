package ecommece.app.backend.model;

import ecommece.app.backend.SyncConstants;
import lombok.Getter;
import lombok.Setter;

public class SyncStatus {

    @Getter @Setter
    private String overallSyncStatus = SyncConstants.SYNC_NA;

    @Getter @Setter
    private String bigCommerceSyncStatus = SyncConstants.SYNC_NA;

    @Getter @Setter
    private String bigCommerceLastUpdate = "";

    @Getter @Setter
    private String bigCommerceFSSyncStatus = SyncConstants.SYNC_NA;

    @Getter @Setter
    private String bigCommerceFSLastUpdate = "";

    @Getter @Setter
    private String vendHQSyncStatus = SyncConstants.SYNC_NA;

    @Getter @Setter
    private String vendHQLastUpdate = "";

    @Getter @Setter
    private String amazonUsStatus = SyncConstants.SYNC_NA;

    @Getter @Setter
    private String amazonUsLastUpdate = "";

    @Getter @Setter
    private String amazonCaStatus = SyncConstants.SYNC_NA;

    @Getter @Setter
    private String amazonCaLastUpdate = "";
}
