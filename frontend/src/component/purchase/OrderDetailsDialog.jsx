import React, { Component } from 'react'
import {connect} from "react-redux";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {createPurchaseOrder} from "../../store/actions/purchaseActions";
import {Fieldset} from "primereact/fieldset";
import {Card} from "primereact/card";
import {Column} from "primereact/column";
import {DataTable} from "primereact/datatable";
import ProductSelectDialog from "./ProductSelectDialog";
import CreateOrderDialog from "./CreateOrderDialog";

class OrderDetailsDialog extends Component {

    constructor() {
        super();
        this.hideDialog = this.hideDialog.bind(this);
        this.onSave = this.onSave.bind(this);
        this.resetInputs = this.resetInputs.bind(this);
        this.successHandler = this.successHandler.bind(this);
        this.errorHandler = this.errorHandler.bind(this);
        this.state = {
            products: [],
            displayProductSelect: false
        }
    }

    resetInputs(){
        this.setState({
            products: [],
            displayProductSelect: false
        });
    }

    onSave(){
        let purchaseOrder = {
            createdBy: "garner"
        }
        //this.props.createPurchaseOrder(purchaseOrder, this.successHandler, this.errorHandler);
    }

    errorHandler(){
        console.log("error handler ");
    }

    successHandler(){
        this.resetInputs();
        //this.props.createOrderSuccessful();
    }

    hideDialog(){
        this.props.onHideEvent();
        this.resetInputs();
    }

    render() {

        let dialogFooter = <div className="ui-dialog-buttonpane p-clearfix">
            <Button label="Close" icon="pi pi-times" onClick={this.hideDialog}/>
        </div>;

        let columnCss = {whiteSpace: 'nowrap', textAlign:'center'};

        let productsDialogHeader = <div className="p-grid p-fluid">
            <div className="p-col-2">
                <Button label="Add Product" onClick={() => this.setState({displayProductSelect: true})}/>
            </div>
            <div className="p-col-8"></div>
        </div>;

        return (
            <Dialog visible={this.props.visibleProperty} maximized={true} modal={true}
                    footer={dialogFooter} onHide={this.hideDialog} showHeader={false} >
                {
                    this.props.order &&
                        <div className="container" style={{width:'1200px'}}>
                            <div className="p-grid p-fluid container">
                                <div className="p-col-12">
                                    <Fieldset legend="Order Details">
                                        <div className="p-grid p-fluid">
                                            <div className="p-col-1 labelText">Order No</div>
                                            <div className="p-col-1">#{this.props.order.id}</div>
                                            <div className="p-col-1 labelText">Created By</div>
                                            <div className="p-col-1">{this.props.order.createdBy}</div>
                                            <div className="p-col-1 labelText">Create Date</div>
                                            <div className="p-col-2">{this.props.order.createDate}</div>
                                            <div className="p-col-1 labelText">Supplier</div>
                                            <div className="p-col-1">{this.props.order.supplier}</div>
                                        </div>
                                    </Fieldset>
                                </div>
                                <div className="p-col-12">
                                    <DataTable value={this.props.orderProducts} paginator={false} header={productsDialogHeader}
                                               selectionMode="single">
                                        <Column bodyStyle={columnCss} field="sku" header="SKU" style={{width:'170px'}}/>
                                        <Column bodyStyle={columnCss} field="name" header="Name"/>
                                        <Column bodyStyle={columnCss} field="costPrice" header="Cost Price" style={{width:'200px'}}/>
                                        <Column bodyStyle={columnCss} field="orderedQuantity" header="Ordered Quantity" style={{width:'200px'}}/>
                                    </DataTable>
                                </div>
                                <div className="p-col-12">
                                    <div className="p-grid p-justify-end">
                                        <div className="p-col-4">
                                            <Card>
                                                <div className="p-grid p-fluid">
                                                    <div className="p-col-6">Sales Tax</div>
                                                    <div className="p-col-6">#{this.props.order.salesTax}</div>
                                                    <div className="p-col-6">Discount</div>
                                                    <div className="p-col-6">#{this.props.order.discount}</div>
                                                    <div className="p-col-6">Shipping</div>
                                                    <div className="p-col-6">#{this.props.order.shipping}</div>
                                                    <div className="p-col-6">Order Total</div>
                                                    <div className="p-col-6">#{this.props.order.orderTotal}</div>
                                                </div>
                                            </Card>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <ProductSelectDialog visibleProperty={this.state.displayProductSelect}
                                                 onHideEvent={() => this.setState({displayProductSelect: false})}  />
                        </div>
                }
            </Dialog>
        )
    }
}

const mapStateToProps = state => {
    return {
        order: state.purchase.selectedOrder,
        orderProducts: state.purchase.selectedOrderProducts
    };
};

const mapDispatchToProps = dispatch => {
    return {
        createPurchaseOrder: (purchaseOrder,successHandler, errorHandler) => dispatch(createPurchaseOrder(purchaseOrder,successHandler, errorHandler))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrderDetailsDialog);