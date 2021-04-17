import React, { Component } from 'react'
import { connect } from "react-redux";
import { getPurchaseOrders, getPurchaseOrderById} from "../../store/actions/purchaseActions"
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Growl} from "primereact/growl";
import CreateOrderDialog from "./CreateOrderDialog";
import {Button} from "primereact/button";
import OrderDetailsDialog from "./OrderDetailsDialog";

class PurchaseOrders extends Component {
    constructor() {
        super();
        this.state= { };
        this.createNewOrder = this.createNewOrder.bind(this);
        this.setGrowlMessage = this.setGrowlMessage.bind(this);
        this.createOrderSuccessful = this.createOrderSuccessful.bind(this);
        this.openOrderDetails = this.openOrderDetails.bind(this);
        this.editButtonBody = this.editButtonBody.bind(this);
    }

    componentDidMount() {
        this.props.getPurchaseOrders();
    }

    // opens new dialog to create a purchase order
    createNewOrder(){
        this.setState({
            displayCreateDialog:true
        });
    }

    // opens new dialog to add and delete products and modify other details
    openOrderDetails(orderId){
        if(orderId){
            this.props.getPurchaseOrderById(orderId);
        }
        this.setState({
            displayDetailsDialog:true
        });
    }

    setGrowlMessage(messages){
        this.growl.show(messages);
    }

    createOrderSuccessful(){
        this.growl.show({severity: 'success', summary: 'Success', detail: 'Purchase order created'});
        this.setState({
            displayDetailsDialog:true,
            displayCreateDialog:false
        });
    }

    editButtonBody(rowData) {
        return (
            <Button type="button" icon="pi pi-folder-open" className="p-button-secondary" onClick={() => this.openOrderDetails(rowData.id)}></Button>
        );
    }

    render() {
        let header = <div className="p-grid p-fluid">
            <div className="p-col-2">
                <Button label="Create New" icon="pi pi-plus"  onClick={this.createNewOrder}/>
            </div>
            <div className="p-col-10">
                <span>Purchase Orders</span>
            </div>
        </div>;

        let columnCss = {whiteSpace: 'nowrap', textAlign:'center'};
        return (
            <div>
                <Growl ref={(el) => this.growl = el} />
                {
                    this.props.orders &&
                    <div className="content-section implementation">
                        <DataTable value={this.props.orders} paginator={true} rows={10} header={header}
                                   paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                   currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries">
                            <Column bodyStyle={columnCss} field="id" header="Id"/>
                            <Column bodyStyle={columnCss} field="supplier" header="Supplier"/>
                            <Column bodyStyle={columnCss} field="orderTotal" header="Total Price"/>
                            <Column bodyStyle={columnCss} field="createDate" header="Create Date"/>
                            <Column bodyStyle={columnCss} field="status" header="Status"/>
                            <Column body={this.editButtonBody} headerStyle={{width: '4em', textAlign: 'center'}} bodyStyle={{textAlign: 'center', overflow: 'visible'}}   />
                        </DataTable>
                        <CreateOrderDialog visibleProperty={this.state.displayCreateDialog} createOrderSuccessful={this.createOrderSuccessful}
                            onHideEvent={() => this.setState({displayCreateDialog: false})} />
                        <OrderDetailsDialog visibleProperty={this.state.displayDetailsDialog} growl={this.growl}
                                           onHideEvent={() => this.setState({displayDetailsDialog: false})} />

                    </div>
                }
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        orders: state.purchase.orders
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getPurchaseOrders: () => dispatch(getPurchaseOrders()),
        getPurchaseOrderById: (id) => dispatch(getPurchaseOrderById(id))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(PurchaseOrders);