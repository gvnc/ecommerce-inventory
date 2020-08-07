import React, { Component } from 'react'
import { connect } from "react-redux";
import { getInventoryCounts, getInventoryCountById, setInventoryCountById } from "../../store/actions/inventoryCountActions";

import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Button} from "primereact/button";
import EditInventoryCountDialog from "./EditInventoryCountDialog";

class InventoryCounts extends Component {

    constructor() {
        super();
        this.state= { };
        this.createNew = this.createNew.bind(this);
        this.openInventoryCountDialog = this.openInventoryCountDialog.bind(this);
        this.editButtonBody = this.editButtonBody.bind(this);
        this.editButtonClicked = this.editButtonClicked.bind(this);
    }

    componentDidMount() {
        this.props.getInventoryCounts();
    }

    // opens new dialog to create a inventory count
    createNew(){
        let data = {
            inventoryCount: {
                id: null,
                name:"",
                includeInactive: false,
                partialCount: false
            },
            productList: []
        }
        this.props.setInventoryCountById(data);
        this.setState({
            displayDetailsDialog:true
        });
    }

    // opens new dialog to save or update inventory count
    openInventoryCountDialog(id){
        if(id){
            this.props.getInventoryCountById(id);
        }
        this.setState({
            displayDetailsDialog:true
        });
    }

    editButtonClicked(status, id){
        if(status === "PLANNED") {
            this.openInventoryCountDialog(id);
        } else {
            this.props.history.push("/inventoryCountInProgress/" + id)
        }
    }

    editButtonBody(rowData) {
        return <Button type="button" icon="pi pi-cog" className="p-button-secondary"
                       onClick={() => this.editButtonClicked(rowData.status, rowData.id)}></Button>;
    }

    render() {
        let header = <div className="p-grid p-fluid">
            <div className="p-col-2">
                <Button label="Create" icon="pi pi-check" onClick={this.createNew}/>
            </div>
            <div className="p-col-10">
                <span>Inventory Counts</span>
            </div>
        </div>;

        let columnCss = {whiteSpace: 'nowrap', textAlign:'center'};
        return (
            <div>
                {
                    this.props.inventoryCounts &&
                    <div className="content-section implementation">
                        <DataTable value={this.props.inventoryCounts} paginator={true} rows={10} header={header}
                                   paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                   currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries">
                            <Column bodyStyle={columnCss} field="id" header="Id"/>
                            <Column bodyStyle={columnCss} field="name" header="Name"/>
                            <Column bodyStyle={columnCss} field="createDate" header="Create Date"/>
                            <Column bodyStyle={columnCss} field="status" header="Status"/>
                            <Column body={this.editButtonBody} headerStyle={{width: '4em', textAlign: 'center'}} bodyStyle={{textAlign: 'center', overflow: 'visible'}}   />
                        </DataTable>
                        <EditInventoryCountDialog visibleProperty={this.state.displayDetailsDialog} history={this.props.history}
                                                  onHideEvent={() => this.setState({displayDetailsDialog: false})} />
                    </div>
                }
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        inventoryCounts: state.inventoryCount.inventoryCounts
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getInventoryCounts: () => dispatch(getInventoryCounts()),
        getInventoryCountById: (id) => dispatch(getInventoryCountById(id)),
        setInventoryCountById: (data) => dispatch(setInventoryCountById(data))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(InventoryCounts);