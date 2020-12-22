import React, { Component } from 'react'
import { connect } from "react-redux";
import { getSyncStatus, startSync, updateSyncStatus} from "../store/actions/syncActions"
import {Card} from "primereact/card";
import {Button} from "primereact/button";

class MarketPlaceSyncStatus extends Component {

    componentDidMount() {
        this.props.getSyncStatus();
        this.getIconImage = this.getIconImage.bind(this);
        this.startSync = this.startSync.bind(this);
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

    startSync(){
        let syncStatus = this.props.syncStatus;
        syncStatus.bigCommerceSyncStatus = "SyncInProgress";
        syncStatus.bigCommerceFSSyncStatus = "SyncInProgress";
        syncStatus.vendHQSyncStatus = "SyncInProgress";
        syncStatus.amazonUsStatus = "SyncInProgress";
        syncStatus.amazonCaStatus = "SyncInProgress";
        syncStatus.squareupSyncStatus = "SyncInProgress";

        this.props.updateSyncStatus(syncStatus, true);
        this.props.startSync();
    }

    render() {
        let syncResult = this.props.syncStatus;
        if(syncResult == null)
            return (<div><h2>Loading, please wait.</h2></div>);

        let cardFooter = <div/>;
        if(this.props.syncInProgress === false) {
            cardFooter = <div style={{textAlign: 'center'}}>
                            <Button label="Start Sync Operation" icon="pi pi-play" onClick={this.startSync}/>
                        </div>;
        }

        return (
            <div className="container">
                <div className="container" style={{width:'500px', marginTop: '50px'}}>
                    <Card title="Market Place Sync Status" footer={cardFooter}>
                        <div className="p-grid p-fluid">
                            <div className="p-col">Big Commerce</div>
                            <div className="p-col">{this.getIconImage(syncResult.bigCommerceSyncStatus)} {syncResult.bigCommerceSyncStatus}</div>
                            <div className="p-col">{syncResult.bigCommerceLastUpdate}</div>
                        </div>
                        <div className="p-grid p-fluid">
                            <div className="p-col">Big Commerce FS</div>
                            <div className="p-col">{this.getIconImage(syncResult.bigCommerceFSSyncStatus)} {syncResult.bigCommerceFSSyncStatus}</div>
                            <div className="p-col">{syncResult.bigCommerceFSLastUpdate}</div>
                        </div>
                        <div className="p-grid p-fluid">
                            <div className="p-col">Vend HQ</div>
                            <div className="p-col">{this.getIconImage(syncResult.vendHQSyncStatus)} {syncResult.vendHQSyncStatus}</div>
                            <div className="p-col">{syncResult.vendHQLastUpdate}</div>
                        </div>
                        {/*
                        <div className="p-grid p-fluid">
                            <div className="p-col">Amazon US</div>
                            <div className="p-col">{this.getIconImage(syncResult.amazonUsStatus)} {syncResult.amazonUsStatus}</div>
                            <div className="p-col">{syncResult.amazonUsLastUpdate}</div>
                        </div>
                        */}
                        <div className="p-grid p-fluid">
                            <div className="p-col">Amazon CA</div>
                            <div className="p-col">{this.getIconImage(syncResult.amazonCaStatus)} {syncResult.amazonCaStatus}</div>
                            <div className="p-col">{syncResult.amazonCaLastUpdate}</div>
                        </div>
                        { // remove comment out to enable square
                            /*
                        <div className="p-grid p-fluid">
                            <div className="p-col">SquareUp</div>
                            <div className="p-col">{this.getIconImage(syncResult.squareupSyncStatus)} {syncResult.squareupSyncStatus}</div>
                            <div className="p-col">{syncResult.squareupLastUpdate}</div>
                        </div>
                             */
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
        updateSyncStatus: (syncData, syncInProgress) => dispatch(updateSyncStatus(syncData, syncInProgress))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(MarketPlaceSyncStatus);