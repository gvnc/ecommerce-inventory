import React, { Component } from 'react'
import {connect} from "react-redux";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {updateSelectedPurchaseOrder, updateSelectedPOProduct, savePurchaseOrder, setPurchaseOrder, deleteSelectedProduct} from "../../store/actions/purchaseActions";
import {Fieldset} from "primereact/fieldset";
import {Card} from "primereact/card";
import {Column} from "primereact/column";
import {DataTable} from "primereact/datatable";
import ProductSelectDialog from "./ProductSelectDialog";
import {InputText} from "primereact/inputtext";

class OrderDetailsDialog extends Component {

    constructor() {
        super();
        this.hideDialog = this.hideDialog.bind(this);
        this.savePurchaseOrder = this.savePurchaseOrder.bind(this);
        this.resetInputs = this.resetInputs.bind(this);
        this.successHandler = this.successHandler.bind(this);
        this.errorHandler = this.errorHandler.bind(this);

        this.onEditorValueChange = this.onEditorValueChange.bind(this);
        this.inputTextEditor = this.inputTextEditor.bind(this);
        this.landedCostEditor = this.landedCostEditor.bind(this);
        this.deleteButtonBody = this.deleteButtonBody.bind(this);

        this.state = {
            displayProductSelect: false
        }
    }

    resetInputs(){
        this.setState({
            displayProductSelect: false
        });
    }

    savePurchaseOrder(){
        this.props.savePurchaseOrder(this.props.order, this.props.orderProducts, this.successHandler, this.errorHandler);
    }

    errorHandler(message){
        this.props.growl.show({severity: 'error', summary: 'Error', detail: message});
    }

    successHandler(message){
        this.props.growl.show({severity: 'success', summary: 'Success', detail: message});
    }

    hideDialog(){
        this.props.onHideEvent();
        this.props.setPurchaseOrder(null);
    }

    onEditorValueChange(props, value) {
        this.props.updateSelectedPOProduct(props.rowData.sku, props.field, value);
    }

    inputTextEditor(props, field) {
        if(field === "costPrice")
            return <InputText type="text" value={props.rowData[field]} onChange={(e) => this.onEditorValueChange(props, e.target.value)} keyfilter = {/^\d*\.?\d*$/} />;
        else
            return <InputText type="text" value={props.rowData[field]} onChange={(e) => this.onEditorValueChange(props, e.target.value)} keyfilter = "int" />;
    }

    landedCostEditor(rowData, expensePerProduct) {
        let landedPrice = Number(rowData.costPrice) + Number(expensePerProduct);
        return <span>{landedPrice}</span>;
    }

    deleteButtonBody(rowData) {
        return (
            <Button type="button" icon="pi pi-minus" className="p-button-secondary" onClick={() => this.props.deleteSelectedProduct(rowData.sku)}></Button>
        );
    }

    render() {
        let totalExpenses = 0;
        if(this.props.order){
            totalExpenses = totalExpenses + Number(this.props.order.salesTax);
            totalExpenses = totalExpenses + Number(this.props.order.brokerage);
            totalExpenses = totalExpenses + Number(this.props.order.discount);
            totalExpenses = totalExpenses + Number(this.props.order.duties);
            totalExpenses = totalExpenses + Number(this.props.order.shipping);
        }
        let totalProducts = 0;
        let totalProductCost = 0;
        if(this.props.orderProducts){
            this.props.orderProducts.forEach(function(p){
                totalProducts = totalProducts + Number(p.orderedQuantity);
                totalProductCost = totalProductCost + (Number(p.orderedQuantity) * Number(p.costPrice));
            });
        }
        let expensePerProduct = 0;
        if(totalProducts > 0)
            expensePerProduct = (totalExpenses / totalProducts).toFixed(2);

        let orderTotal = totalProductCost + totalExpenses;

        let dialogFooter =  <div className="ui-dialog-buttonpane p-clearfix">
                                <Button label="Submit" icon="pi pi-check" onClick={this.hideDialog}/>
                                <Button label="Save" icon="pi pi-pencil" onClick={this.savePurchaseOrder}/>
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
                                            <div className="p-col-2 labelText">Order No</div>
                                            <div className="p-col-2">#{this.props.order.id}</div>
                                            <div className="p-col-2 labelText">Created By</div>
                                            <div className="p-col-2">{this.props.order.createdBy}</div>
                                            <div className="p-col-2 labelText">Create Date</div>
                                            <div className="p-col-2">{this.props.order.createDate}</div>
                                            <div className="p-col-2 labelText">Supplier</div>
                                            <div className="p-col-2">
                                                <InputText id="supplier" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "supplier", e.target.value)}}
                                                           value={this.props.order.supplier} style={{width:'200px'}} />
                                            </div>
                                        </div>
                                    </Fieldset>
                                </div>
                                <div className="p-col-12">
                                    <DataTable value={this.props.orderProducts} paginator={false} header={productsDialogHeader} editable={true}>
                                        <Column bodyStyle={columnCss} field="sku" header="SKU" style={{width:'170px'}}/>
                                        <Column bodyStyle={columnCss} field="name" header="Name"/>
                                        <Column bodyStyle={columnCss} field="costPrice" header="Cost Price" style={{width:'100px'}} editor={(props) => this.inputTextEditor(props, 'costPrice')} />
                                        <Column bodyStyle={columnCss} header="Landed Price" style={{width:'100px'}} body={(rowData) => this.landedCostEditor(rowData, expensePerProduct)} />
                                        <Column bodyStyle={columnCss} field="orderedQuantity" header="Ordered Quantity" style={{width:'100px'}} editor={(props) => this.inputTextEditor(props, 'orderedQuantity')} />
                                        <Column body={this.deleteButtonBody} headerStyle={{width: '4em', textAlign: 'center'}} bodyStyle={{textAlign: 'center', overflow: 'visible'}}   />
                                    </DataTable>
                                </div>
                                <div className="p-col-12">
                                    <div className="p-grid p-justify-end">
                                        <div className="p-col-4">
                                            <Card>
                                                <div className="p-grid p-fluid">
                                                    <div className="p-col-6">Sales Tax</div>
                                                    <div className="p-col-6">
                                                        <InputText id="salesTax" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "salesTax", e.target.value)}}
                                                                   value={this.props.order.salesTax} style={{width:'100px'}} keyfilter = {/^\d*\.?\d*$/}  />
                                                    </div>
                                                    <div className="p-col-6">Brokerage</div>
                                                    <div className="p-col-6">
                                                        <InputText id="brokerage" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "brokerage", e.target.value)}}
                                                                   value={this.props.order.brokerage} style={{width:'100px'}} keyfilter = {/^\d*\.?\d*$/}  /></div>
                                                    <div className="p-col-6">Discount</div>
                                                    <div className="p-col-6">
                                                        <InputText id="discount" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "discount", e.target.value)}}
                                                                   value={this.props.order.discount} style={{width:'100px'}} keyfilter = {/^\d*\.?\d*$/}  /></div>
                                                    <div className="p-col-6">Duties</div>
                                                    <div className="p-col-6">
                                                        <InputText id="duties" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "duties", e.target.value)}}
                                                                   value={this.props.order.duties} style={{width:'100px'}} keyfilter = {/^\d*\.?\d*$/}  /></div>
                                                    <div className="p-col-6">Shipping</div>
                                                    <div className="p-col-6">
                                                        <InputText id="shipping" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "shipping", e.target.value)}}
                                                                   value={this.props.order.shipping} style={{width:'100px'}} keyfilter = {/^\d*\.?\d*$/}  /></div>
                                                    <div className="p-col-6 labelText">Order Total</div>
                                                    <div className="p-col-6 labelText">{orderTotal}</div>
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
        updateSelectedPurchaseOrder: (id, propertyName, propertyValue) => dispatch(updateSelectedPurchaseOrder(id, propertyName, propertyValue)),
        updateSelectedPOProduct: (sku, propertyName, propertyValue) => dispatch(updateSelectedPOProduct(sku, propertyName, propertyValue)),
        savePurchaseOrder: (purchaseOrder, productList, successHandler, errorHandler) => dispatch(savePurchaseOrder(purchaseOrder, productList, successHandler, errorHandler)),
        setPurchaseOrder: (data) => dispatch(setPurchaseOrder(data)),
        deleteSelectedProduct: (sku) => dispatch(deleteSelectedProduct(sku))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrderDetailsDialog);