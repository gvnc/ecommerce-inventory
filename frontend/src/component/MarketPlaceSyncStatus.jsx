import React, { Component } from 'react'
import { connect } from "react-redux";
import { getSyncStatus, startSync, updateSyncStatus, syncFromMaster} from "../store/actions/syncActions"
import {Card} from "primereact/card";
import {Button} from "primereact/button";
import {Growl} from "primereact/growl";

const SYNC_IN_PROGRESS = "SyncInProgress";

class MarketPlaceSyncStatus extends Component {

    componentDidMount() {
        this.props.getSyncStatus();
        this.getIconImage = this.getIconImage.bind(this);
        this.startSync = this.startSync.bind(this);
        this.setSyncStatusInPending = this.setSyncStatusInPending.bind(this);
        this.syncFromMaster = this.syncFromMaster.bind(this);
        this.isSyncInProgress = this.isSyncInProgress.bind(this);
    }


    getIconImage(syncResult){
        if(syncResult === "SyncCompleted")
            return <img width={20} height={20} alt="" src={require('../assets/green.png')} />;
        if(syncResult === "SyncFailed")
            return <img width={20} height={20} alt="" src={require('../assets/red.png')} />;
        if(syncResult === "SyncInProgress")
            return <img width={20} height={20} alt="" src={require('../assets/loading.png')} />;

        return <img width={20} height={20} alt="" src={require('../assets/yellow.png')} />;
    }

    isSyncInProgress(){
        if(process.env.REACT_APP_SHOW_BC && this.props.syncStatus.bigCommerceSyncStatus === SYNC_IN_PROGRESS)
            return true;
        if(process.env.REACT_APP_SHOW_BCFS && this.props.syncStatus.bigCommerceFSSyncStatus === SYNC_IN_PROGRESS)
            return true;
        if(process.env.REACT_APP_SHOW_VEND && this.props.syncStatus.vendHQSyncStatus === SYNC_IN_PROGRESS)
            return true;
        if(process.env.REACT_APP_SHOW_AMCA && this.props.syncStatus.amazonCaStatus === SYNC_IN_PROGRESS)
            return true;
        if(process.env.REACT_APP_SHOW_HELCIM && this.props.syncStatus.helcimSyncStatus === SYNC_IN_PROGRESS)
            return true;
    }

    setSyncStatusInPending(){
        let syncStatus = this.props.syncStatus;
        syncStatus.bigCommerceSyncStatus = SYNC_IN_PROGRESS;
        syncStatus.bigCommerceFSSyncStatus = SYNC_IN_PROGRESS;
        syncStatus.vendHQSyncStatus = SYNC_IN_PROGRESS;
        syncStatus.helcimSyncStatus = SYNC_IN_PROGRESS;
        syncStatus.amazonUsStatus = SYNC_IN_PROGRESS;
        syncStatus.amazonCaStatus = SYNC_IN_PROGRESS;
        syncStatus.squareupSyncStatus = SYNC_IN_PROGRESS;

        this.props.updateSyncStatus(syncStatus, true);
    }

    startSync(){
        if(this.isSyncInProgress()){
            this.growl.show({severity: 'error', summary: 'Error', detail: 'Sync is still in progress !'});
            return;
        }
        this.setSyncStatusInPending();
        this.props.startSync();
    }

    syncFromMaster(){
        if(this.isSyncInProgress()){
            this.growl.show({severity: 'error', summary: 'Error', detail: 'Sync is still in progress !'});
            return;
        }
        this.setSyncStatusInPending();
        this.props.syncFromMaster();
    }

    render() {
        let syncResult = this.props.syncStatus;
        if(syncResult == null)
            return (<div><h2>Loading, please wait.</h2></div>);

        let cardFooter = <div/>;
        if(this.props.syncInProgress === false) {
            cardFooter = <div style={{textAlign: 'center'}}>
                            <Button label="Start Sync Operation" icon="pi pi-play" onClick={this.startSync}/>
                            {
                                process.env.REACT_APP_SHOW_SYNC_FROM_MASTER &&
                                    <Button label="Sync From Master" icon="pi pi-save"
                                            onClick={this.syncFromMaster} style={{marginLeft: "10px"}} />
                            }
                        </div>;
        }

        return (
            <div className="container">
                <Growl ref={(el) => this.growl = el} />
                <div className="container" style={{width:'500px', marginTop: '50px'}}>
                    <Card title="Market Place Sync Status" footer={cardFooter}>
                        {
                            process.env.REACT_APP_SHOW_BC &&
                            <div className="p-grid p-fluid">
                                <div className="p-col">Big Commerce</div>
                                <div
                                    className="p-col">{this.getIconImage(syncResult.bigCommerceSyncStatus)} {syncResult.bigCommerceSyncStatus}</div>
                                <div className="p-col">{syncResult.bigCommerceLastUpdate}</div>
                            </div>
                        }
                        {
                            process.env.REACT_APP_SHOW_BCFS &&
                            <div className="p-grid p-fluid">
                                <div className="p-col">Big Commerce FS</div>
                                <div
                                    className="p-col">{this.getIconImage(syncResult.bigCommerceFSSyncStatus)} {syncResult.bigCommerceFSSyncStatus}</div>
                                <div className="p-col">{syncResult.bigCommerceFSLastUpdate}</div>
                            </div>
                        }
                        {
                            process.env.REACT_APP_SHOW_HELCIM &&
                            <div className="p-grid p-fluid">
                                <div className="p-col">Helcim</div>
                                <div className="p-col">{this.getIconImage(syncResult.helcimSyncStatus)} {syncResult.helcimSyncStatus}</div>
                                <div className="p-col">{syncResult.helcimLastUpdate}</div>
                            </div>
                        }
                        {
                            process.env.REACT_APP_SHOW_VEND &&
                            <div className="p-grid p-fluid">
                                <div className="p-col">Vend HQ</div>
                                <div className="p-col">{this.getIconImage(syncResult.vendHQSyncStatus)} {syncResult.vendHQSyncStatus}</div>
                                <div className="p-col">{syncResult.vendHQLastUpdate}</div>
                            </div>
                        }
                        {
                            process.env.REACT_APP_SHOW_AMUS &&
                            <div className="p-grid p-fluid">
                                <div className="p-col">Amazon US</div>
                                <div
                                    className="p-col">{this.getIconImage(syncResult.amazonUsStatus)} {syncResult.amazonUsStatus}</div>
                                <div className="p-col">{syncResult.amazonUsLastUpdate}</div>
                            </div>
                        }

                        {
                            process.env.REACT_APP_SHOW_AMCA &&
                            <div className="p-grid p-fluid">
                                <div className="p-col">Amazon CA</div>
                                <div
                                    className="p-col">{this.getIconImage(syncResult.amazonCaStatus)} {syncResult.amazonCaStatus}</div>
                                <div className="p-col">{syncResult.amazonCaLastUpdate}</div>
                            </div>
                        }
                        {
                            process.env.REACT_APP_SHOW_SQUARE &&
                            <div className="p-grid p-fluid">
                                <div className="p-col">SquareUp</div>
                                <div className="p-col">{this.getIconImage(syncResult.squareupSyncStatus)} {syncResult.squareupSyncStatus}</div>
                                <div className="p-col">{syncResult.squareupLastUpdate}</div>
                            </div>
                        }
                    </Card>
                </div>
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        syncStatus: state.syncMarkets.syncStatus,
        syncInProgress: state.syncMarkets.syncInProgress
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getSyncStatus: () => dispatch(getSyncStatus()),
        startSync: () => dispatch(startSync()),
        syncFromMaster: () => dispatch(syncFromMaster()),
        updateSyncStatus: (syncData, syncInProgress) => dispatch(updateSyncStatus(syncData, syncInProgress))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(MarketPlaceSyncStatus);