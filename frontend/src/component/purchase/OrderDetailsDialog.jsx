import React, { Component } from 'react'
import {connect} from "react-redux";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {updateSelectedPurchaseOrder, updateSelectedPOProduct, savePurchaseOrder,
    setPurchaseOrder, deleteSelectedProduct, deletePurchaseOrderProduct,
    submitPurchaseOrder, receivePurchaseProducts, cancelPurchaseOrder,
    deletePurchaseOrder } from "../../store/actions/purchaseActions";
import {Fieldset} from "primereact/fieldset";
import {Card} from "primereact/card";
import {Column} from "primereact/column";
import {DataTable} from "primereact/datatable";
import ProductSelectDialog from "./ProductSelectDialog";
import {InputText} from "primereact/inputtext";
import {Checkbox} from 'primereact/checkbox';
import ConfirmationDialog from "../ConfirmationDialog";

class OrderDetailsDialog extends Component {

    constructor() {
        super();
        this.hideDialog = this.hideDialog.bind(this);
        this.savePurchaseOrder = this.savePurchaseOrder.bind(this);
        this.resetInputs = this.resetInputs.bind(this);
        this.successHandler = this.successHandler.bind(this);
        this.errorHandler = this.errorHandler.bind(this);

        this.onEditorValueChange = this.onEditorValueChange.bind(this);
        this.draftInputTextEditor = this.draftInputTextEditor.bind(this);
        this.landedCostEditor = this.landedCostEditor.bind(this);
        this.deleteButtonBody = this.deleteButtonBody.bind(this);
        this.deleteProductFromList = this.deleteProductFromList.bind(this);
        this.submitPurchaseOrder = this.submitPurchaseOrder.bind(this);
        this.receiveInputText = this.receiveInputText.bind(this);
        this.receiveAllCheckEvent = this.receiveAllCheckEvent.bind(this);
        this.setReceiveValue = this.setReceiveValue.bind(this);
        this.receiveProducts = this.receiveProducts.bind(this);
        this.cancelOrder = this.cancelOrder.bind(this);
        this.deleteOrder = this.deleteOrder.bind(this);
        this.deletedSuccessHandler = this.deletedSuccessHandler.bind(this);

        this.state = {
            displayProductSelect: false,
            checked: false,
            receiveList: [],
            displayPODeleteConfirmation: false,
            displayPOCancelConfirmation: false
        }
    }

    resetInputs(){
        this.setState({
            displayProductSelect: false,
            checked: false,
            receiveList: [],
            displayPODeleteConfirmation: false,
            displayPOCancelConfirmation: false
        });
    }

    savePurchaseOrder(){
        this.props.savePurchaseOrder(this.props.order, this.props.orderProducts, this.successHandler, this.errorHandler);
    }

    receiveProducts(){
        this.props.receivePurchaseProducts(this.props.order.id, this.state.receiveList, this.successHandler, this.errorHandler);
        let receiveList = [];
        this.props.orderProducts.forEach(function(p){
            receiveList.push({
                sku: p.sku,
                receivedQuantity: "0"
            });
        });
        this.setState({
            receiveList: receiveList,
            checked: false
        });
    }

    submitPurchaseOrder(){
        if(this.props.orderProducts && this.props.orderProducts.length > 0){
            // check all products have quantity greater than 0
            console.log(JSON.stringify(this.props.orderProducts));
            let quantityIndex = this.props.orderProducts.findIndex(item => item.orderedQuantity === "" || item.orderedQuantity === "0" || item.orderedQuantity === 0);
            console.log("quantiyIndex " + quantityIndex);
            if(quantityIndex === -1){
                this.props.submitPurchaseOrder(this.props.order, this.props.orderProducts, this.successHandler, this.errorHandler);
            }
        }

    }


    errorHandler(message){
        this.props.growl.show({severity: 'error', summary: 'Error', detail: message});
    }

    successHandler(message){
        this.props.growl.show({severity: 'success', summary: 'Success', detail: message});
    }

    hideDialog(){
        this.props.onHideEvent();
        this.resetInputs();
        this.props.setPurchaseOrder(null);
    }

    onEditorValueChange(props, value) {
        if(props.field === "receivedQuantity"){
            if(Number(value) > Number(props.rowData.remainingQuantity))
                return;
            let remainingQuantity = Number(props.rowData.remainingQuantity) - Number(value);
            this.props.updateSelectedPOProduct(props.rowData.sku, "remainingQuantity", remainingQuantity);
        }

        if(props.field === "orderedQuantity")
            this.props.updateSelectedPOProduct(props.rowData.sku, "remainingQuantity", value);

        this.props.updateSelectedPOProduct(props.rowData.sku, props.field, value);
    }

    draftInputTextEditor(props, field) {
        let orderStatus = this.props.order ? this.props.order.status : "";
        if(orderStatus !== 'DRAFT'){
            return props.rowData[field];
        }

        if(field === "costPrice")
            return <InputText type="text" value={props.rowData[field]} onChange={(e) => this.onEditorValueChange(props, e.target.value)} keyfilter = {/^\d*\.?\d*$/} />;
        else
            return <InputText type="text" value={props.rowData[field]} onChange={(e) => this.onEditorValueChange(props, e.target.value)} keyfilter = "int" />;
    }

    setReceiveValue(sku, remainingQuantity, receivedQuantity){
        if(Number(receivedQuantity) > Number(remainingQuantity))
            return;

        let receiveItem = this.state.receiveList.find(item => item.sku === sku);
        if(receiveItem) {
            let receiveList = this.state.receiveList.map((item, index) => {
                if (item.sku === sku) {
                    return {
                        sku: sku,
                        receivedQuantity: receivedQuantity
                    };
                }
                return item;
            });
            this.setState({receiveList: receiveList});
        } else {
            let receiveList = this.state.receiveList;
            receiveItem = {
                sku: sku,
                receivedQuantity: receivedQuantity
            }
            receiveList.push(receiveItem);
            this.setState({receiveList: receiveList});
        }
    }

    receiveInputText(rowData) {
        let receiveItem = this.state.receiveList.find(item => item.sku === rowData.sku);
        let receiveValue = receiveItem ? receiveItem.receivedQuantity : "0";
        return <InputText type="text" value={receiveValue} onChange={(e) => this.setReceiveValue(rowData.sku, rowData.remainingQuantity, e.target.value)} keyfilter = "int" />;
    }

    landedCostEditor(rowData, expensePerProduct) {
        let landedPrice = Number(rowData.costPrice) + Number(expensePerProduct);
        return <span>{landedPrice}</span>;
    }

    deleteProductFromList(product){
        if(product.id)// if persisted remove from db and redux store
            this.props.deletePurchaseOrderProduct(this.props.order.id, product, this.successHandler, this.errorHandler);
        else  // if not persisted to db, only remove from redux store
            this.props.deleteSelectedProduct(product.sku);
    }

    deleteButtonBody(rowData) {
        return (
            <Button type="button" icon="pi pi-minus" className="p-button-secondary" onClick={() => this.deleteProductFromList(rowData)}></Button>
        );
    }

    receiveAllCheckEvent(e){
        this.setState({checked: e.checked});
        let receiveList = [];
        if(e.checked === true){
            this.props.orderProducts.forEach(function(p){
                receiveList.push({
                   sku: p.sku,
                   receivedQuantity: p.remainingQuantity
                });
            });
        } else {
            this.props.orderProducts.forEach(function(p){
                receiveList.push({
                    sku: p.sku,
                    receivedQuantity: "0"
                });
            });
        }
        this.setState({receiveList: receiveList});
    }

    cancelOrder(){
        this.props.cancelPurchaseOrder(this.props.order.id, this.successHandler, this.errorHandler);
        this.setState({displayPOCancelConfirmation: false})
    }

    deletedSuccessHandler(message){
        this.props.growl.show({severity: 'success', summary: 'Success', detail: message});
        this.hideDialog();
    }

    deleteOrder(){
        this.props.deletePurchaseOrder(this.props.order.id, this.deletedSuccessHandler, this.errorHandler);
    }

    render() {
        let totalExpenses = 0;
        if(this.props.order){
            totalExpenses = totalExpenses + Number(this.props.order.salesTax);
            totalExpenses = totalExpenses + Number(this.props.order.brokerage);
            totalExpenses = totalExpenses - Number(this.props.order.discount);
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

        let orderStatus = this.props.order ? this.props.order.status : "";
        let draftOpts = { };
        if(orderStatus !== 'DRAFT'){
            draftOpts['readOnly'] = 'readOnly';
        }

        let submitButtonOpts = { disabled: 'disabled'};
        if(this.props.orderProducts && this.props.orderProducts.length > 0){
            let quantityIndex = this.props.orderProducts.findIndex(item => item.orderedQuantity === ""
                || item.orderedQuantity === "0" || item.orderedQuantity === 0);
            if(quantityIndex === -1){
                submitButtonOpts = { };
            }
        }

        let dialogFooter =  <div className="ui-dialog-buttonpane p-clearfix">
                        {
                            orderStatus === 'DRAFT' &&
                            <Button label="Submit" icon="pi pi-check" {...submitButtonOpts} onClick={this.submitPurchaseOrder}/>
                        }
                        {
                            orderStatus === 'DRAFT' &&
                            <Button label="Save" icon="pi pi-pencil" onClick={this.savePurchaseOrder}/>
                        }
                        {
                            orderStatus === 'DRAFT' &&
                            <Button label="Delete" icon="pi pi-trash" onClick={() => this.setState({displayPODeleteConfirmation: true})}/>
                        }
                        {
                            orderStatus !== 'DRAFT' &&
                            <Button label="Download PDF" icon="pi pi-download"/>
                        }
                        {
                            orderStatus === 'SUBMITTED' &&
                            <Button label="Cancel Order" icon="pi pi-ban" onClick={() => this.setState({displayPOCancelConfirmation: true})}/>
                        }
                                <Button label="Close" icon="pi pi-times" onClick={this.hideDialog} />
                            </div>;

        let columnCss = {whiteSpace: 'nowrap', textAlign:'center'};

        let productsDialogHeader =
                    <div className="p-grid p-fluid">
                        {
                            orderStatus === 'DRAFT' &&
                            <div className="p-col-2">
                                <Button label="Add Product" onClick={() => this.setState({displayProductSelect: true})}/>
                            </div>
                        }
                        {
                            orderStatus !== 'DRAFT' && orderStatus !== 'COMPLETED' && orderStatus !== "CANCELLED" &&
                            <div className="p-col-2">
                                <Button label="Receive Products" onClick={this.receiveProducts} />
                            </div>
                        }
                        {
                            orderStatus !== 'DRAFT' && orderStatus !== 'COMPLETED' && orderStatus !== "CANCELLED" &&
                            <div className="p-col-2">
                                <Checkbox inputId="allProductsCheck" onChange={e => this.receiveAllCheckEvent(e)} checked={this.state.checked}></Checkbox>
                                <label htmlFor="allProductsCheck" className="p-checkbox-label">receive all</label>
                            </div>
                        }
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
                                            <div className="p-col-2 labelText">Order Status</div>
                                            <div className="p-col-2">{this.props.order.status}</div>
                                            <div className="p-col-2 labelText">Supplier</div>
                                            <div className="p-col-2">
                                                <InputText id="supplier" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "supplier", e.target.value)}}
                                                           value={this.props.order.supplier} style={{width:'200px'}} {...draftOpts} />
                                            </div>
                                        </div>
                                    </Fieldset>
                                </div>
                                <div className="p-col-12">
                                    <DataTable value={this.props.orderProducts} paginator={false} header={productsDialogHeader} editable={true}>
                                        <Column bodyStyle={columnCss} field="sku" header="SKU" style={{width:'170px'}}/>
                                        <Column bodyStyle={columnCss} field="name" header="Name"/>
                                        <Column bodyStyle={columnCss} field="costPrice" header="Cost Price" style={{width:'100px'}} editor={(props) => this.draftInputTextEditor(props, 'costPrice')} />
                                        <Column bodyStyle={columnCss} header="Landed Price" style={{width:'100px'}} body={(rowData) => this.landedCostEditor(rowData, expensePerProduct)} />
                                        <Column bodyStyle={columnCss} field="orderedQuantity" header="Ordered Quantity" style={{width:'100px'}} editor={(props) => this.draftInputTextEditor(props, 'orderedQuantity')} />
                                        {
                                            orderStatus === "DRAFT" &&
                                            <Column body={this.deleteButtonBody} headerStyle={{width: '4em', textAlign: 'center'}} bodyStyle={{textAlign: 'center', overflow: 'visible'}}   />
                                        }
                                        {
                                            orderStatus !== "DRAFT" &&
                                            <Column bodyStyle={columnCss} field="receivedQuantity" header="Received Quantity" style={{width:'100px'}} />
                                        }
                                        {
                                            orderStatus !== "DRAFT" && orderStatus !== "CANCELLED" && orderStatus !== "COMPLETED" &&
                                            <Column bodyStyle={columnCss} header="Receive New" style={{width:'100px'}} body={this.receiveInputText} />
                                        }
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
                                                                   value={this.props.order.salesTax} style={{width:'100px'}} {...draftOpts} keyfilter = {/^\d*\.?\d*$/} />
                                                    </div>
                                                    <div className="p-col-6">Brokerage</div>
                                                    <div className="p-col-6">
                                                        <InputText id="brokerage" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "brokerage", e.target.value)}}
                                                                   value={this.props.order.brokerage} style={{width:'100px'}} {...draftOpts} keyfilter = {/^\d*\.?\d*$/}  /></div>
                                                    <div className="p-col-6">Discount</div>
                                                    <div className="p-col-6">
                                                        <InputText id="discount" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "discount", e.target.value)}}
                                                                   value={this.props.order.discount} style={{width:'100px'}} {...draftOpts} keyfilter = {/^\d*\.?\d*$/}  /></div>
                                                    <div className="p-col-6">Duties</div>
                                                    <div className="p-col-6">
                                                        <InputText id="duties" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "duties", e.target.value)}}
                                                                   value={this.props.order.duties} style={{width:'100px'}} {...draftOpts} keyfilter = {/^\d*\.?\d*$/}  /></div>
                                                    <div className="p-col-6">Shipping</div>
                                                    <div className="p-col-6">
                                                        <InputText id="shipping" onChange={(e) => {this.props.updateSelectedPurchaseOrder(this.props.order.id, "shipping", e.target.value)}}
                                                                   value={this.props.order.shipping} style={{width:'100px'}} {...draftOpts} keyfilter = {/^\d*\.?\d*$/}  /></div>
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

                            <ConfirmationDialog  visibleProperty={this.state.displayPODeleteConfirmation}
                                                 noHandler={() => this.setState({displayPODeleteConfirmation: false})}
                                                 yesHandler={this.deleteOrder}
                                                 message="Do you confirm to delete this purchase order ?" />

                            <ConfirmationDialog  visibleProperty={this.state.displayPOCancelConfirmation}
                                                 noHandler={() => this.setState({displayPOCancelConfirmation: false})}
                                                 yesHandler={this.cancelOrder}
                                                 message="Do you confirm to cancel this purchase order ?" />
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
        deleteSelectedProduct: (sku) => dispatch(deleteSelectedProduct(sku)),
        deletePurchaseOrderProduct: (orderId, product, successHandler, errorHandler) => dispatch(deletePurchaseOrderProduct(orderId, product, successHandler, errorHandler)),
        submitPurchaseOrder: (purchaseOrder, productList, successHandler, errorHandler) => dispatch(submitPurchaseOrder(purchaseOrder, productList, successHandler, errorHandler)),
        receivePurchaseProducts: (orderId, receiveList, successHandler, errorHandler) => dispatch(receivePurchaseProducts(orderId, receiveList, successHandler, errorHandler)),
        cancelPurchaseOrder: (orderId, successHandler, errorHandler) => dispatch(cancelPurchaseOrder(orderId, successHandler, errorHandler)),
        deletePurchaseOrder: (orderId, deletedSuccessHandler, errorHandler)  => dispatch(deletePurchaseOrder(orderId, deletedSuccessHandler, errorHandler))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(OrderDetailsDialog);