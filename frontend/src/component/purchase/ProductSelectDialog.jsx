import React, { Component } from 'react'
import {connect} from "react-redux";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {getProductList} from "../../store/actions/productActions";
import {addSelectedPurchaseProducts} from "../../store/actions/purchaseActions"
import ProductService from "../../service/ProductService";

class ProductSelectDialog extends Component {

    constructor() {
        super();
        this.hideDialog = this.hideDialog.bind(this);
        this.onProductSelect = this.onProductSelect.bind(this);
        this.resetInputs = this.resetInputs.bind(this);
        this.addButtonBody = this.addButtonBody.bind(this);
        this.addSingleProduct = this.addSingleProduct.bind(this);
        this.addSelectedProducts = this.addSelectedProducts.bind(this);
        this.convertProduct = this.convertProduct.bind(this);
        this.getProductDetailsAndAdd = this.getProductDetailsAndAdd.bind(this);
        this.getSelectedProductDetailsAndAdd = this.getSelectedProductDetailsAndAdd.bind(this);

        this.state = {
            selectedProducts: []
        }
    }

    componentDidMount() {
        if (this.props.productList.length === 0) {
            this.props.getProductList();
        }
    }

    resetInputs(){
        this.setState({
            selectedProducts: []
        });
    }

    onProductSelect(){

    }

    hideDialog(){
        this.props.onHideEvent();
        this.resetInputs();
    }

    convertProduct(inventoryProduct){
        // modify inventory product to purchase product model
        let costPrice = 0;
        if(inventoryProduct.bigCommerceProduct !== null)
            costPrice = inventoryProduct.bigCommerceProduct.cost_price;
        else if(inventoryProduct.bigCommerceFSProduct !== null)
            costPrice = inventoryProduct.bigCommerceFSProduct.cost_price;
        else if(inventoryProduct.vendHQProduct !== null)
            costPrice = inventoryProduct.vendHQProduct.supply_price;

        return {
            sku: inventoryProduct.sku,
            name: inventoryProduct.name,
            costPrice: costPrice,
            orderedQuantity: 0,
            receivedQuantity: 0
        }
    }

    getProductDetailsAndAdd(product){
        ProductService.getProductBySku(product.sku, this.addSingleProduct);
    }

    addSingleProduct(product){
        if( product !== undefined && product !== null) {
            let productsArray = [];
            let poProduct = this.convertProduct(product);
            productsArray.push(poProduct);

            this.props.addSelectedPurchaseProducts(productsArray);
        }
    }

    getSelectedProductDetailsAndAdd(product){
        if(this.state.selectedProducts.length > 0){
            let skuList = "";
            this.state.selectedProducts.forEach((p) => skuList = skuList + "," + p.sku );
            ProductService.getProductsBySkuList(skuList.substring(1), this.addSelectedProducts);
        }
    }

    addSelectedProducts(productList){
        if(productList !== undefined && productList !== null) {
            let productsArray = [];
            let convertProduct = this.convertProduct;
            productList.forEach(function (product) {
                let poProduct = convertProduct(product);
                productsArray.push(poProduct);
            })
            if (productsArray.length > 0) {
                this.props.addSelectedPurchaseProducts(productsArray);
            }
        }
    }

    addButtonBody(rowData) {
        return (
            <Button type="button" icon="pi pi-plus" className="p-button-secondary" onClick={() => this.getProductDetailsAndAdd(rowData)}></Button>
        );
    }

    render() {
        let dialogFooter =  <div className="ui-dialog-buttonpane p-clearfix">
                                <Button label="Add Selected Products" onClick={this.getSelectedProductDetailsAndAdd}/>
                            </div>;

        return (
            <Dialog visible={this.props.visibleProperty} header="select from the products" modal={true}
                    footer={dialogFooter} onHide={this.hideDialog} style={{width:'800px'}} >
                <DataTable value={this.props.productList} paginator={true} rows={5} style={{height:'370px'}}
                           selection={this.state.selectedProducts}
                           onSelectionChange={e => this.setState({selectedProducts: e.value})}
                           onRowSelect={this.onProductSelect}
                           paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                           currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
                           headerStyle={{display:'none'}}>
                    <Column selectionMode="multiple" style={{width:'3em'}}/>
                    <Column field="sku" header="Product SKU" filter={true} filterPlaceholder="search sku" filterMatchMode="contains" style={{width:'170px'}} />
                    <Column field="name" header="Product Name" filter={true} filterPlaceholder="search product" filterMatchMode="contains" />
                    <Column body={this.addButtonBody} headerStyle={{width: '4em', textAlign: 'center'}} bodyStyle={{textAlign: 'center', overflow: 'visible'}} />
                </DataTable>
            </Dialog>
        )
    }
}

const mapStateToProps = state => {
    return {
        productsRequested: state.product.productsRequested,
        productList: state.product.productList,
        selectedOrderProducts: state.purchase.selectedOrderProducts
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getProductList: () => dispatch(getProductList()),
        addSelectedPurchaseProducts: (productsArray) => dispatch(addSelectedPurchaseProducts(productsArray))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(ProductSelectDialog);