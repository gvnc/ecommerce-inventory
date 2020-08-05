import React, { Component } from 'react'
import { connect } from "react-redux";
import {commitPriceChange, getProductList, updateBaseProductPrice} from "../../store/actions/productActions"
import {DataTable} from "primereact/datatable";
import {InputText} from "primereact/inputtext";
import {Column} from "primereact/column";
import ProductDetailDialog from "./ProductDetailDialog";
import {Growl} from 'primereact/growl';


class ListProductsBulkEdit extends Component {

    constructor() {
        super();
        this.state = {};

        this.modifiedProducts = {};

        this.setGrowlMessage = this.setGrowlMessage.bind(this);

        this.editorForRowEditing = this.editorForRowEditing.bind(this);
        this.onRowEditorValidator = this.onRowEditorValidator.bind(this);
        this.onRowEditInit = this.onRowEditInit.bind(this);
        this.onRowEditSave = this.onRowEditSave.bind(this);
        this.onRowEditCancel = this.onRowEditCancel.bind(this);
        this.priceFieldRender = this.priceFieldRender.bind(this);
    }

    componentDidMount() {
        if (this.props.productList.length === 0) {
            this.props.getProductList();
        }
    }

    setGrowlMessage(messages){
        this.growl.show(messages);
    }

    syncBigCommercePrice(baseProduct, newValue){
        if(baseProduct.bigCommercePrice !== null)
            baseProduct.bigCommercePrice = newValue;
        if(baseProduct.bigCommerceFSPrice !== null)
            baseProduct.bigCommerceFSPrice = newValue;
        if(baseProduct.vendHQPrice !== null)
            baseProduct.vendHQPrice = newValue;
    }

    syncAmazonPrice(baseProduct, newValue){
        if(baseProduct.amazonCAPrice !== null)
            baseProduct.amazonCAPrice = newValue;
    }

    /* Row Editing */
    onEditorValueChangeForRowEditing(baseProduct, field, value) {
        var floatRegexPattern = /^\d*(\.\d*)?$/;
        if(value === "" || floatRegexPattern.test(value)){
            if(field === "bigCommercePrice" || field === "bigCommerceFSPrice" || field === "vendHQPrice"){
                this.syncBigCommercePrice(baseProduct, value);
            } else {
                this.syncAmazonPrice(baseProduct, value);
            }
            this.props.updateBaseProductPrice(baseProduct);
        }
    }

    editorForRowEditing(props, field) {
        if(props.rowData[field] !== null){
            return <InputText type="text" value={props.rowData[field]}
                              onChange={(e) => this.onEditorValueChangeForRowEditing(props.rowData, field, e.target.value)} />;
        } else {
            return "Not Available";
        }
    }

    isPriceValid(value){
        if(value !== null && (value === "" || value === "." || value.toString().substr(value.length-1) === ".")){
            return false;
        }
        return true;
    }

    onRowEditorValidator(rowData) {
        let isValid = true;
        isValid = isValid && this.isPriceValid(rowData["bigCommercePrice"]);
        isValid = isValid && this.isPriceValid(rowData["bigCommerceFSPrice"]);
        isValid = isValid && this.isPriceValid(rowData["vendHQPrice"]);
        isValid = isValid && this.isPriceValid(rowData["amazonCAPrice"]);
        return isValid;
    }

    onRowEditInit(event) {
        this.modifiedProducts[event.data.sku] = {...event.data};
    }

    onRowEditSave(event) {
        let baseProduct = event.data;

        // validate fields
        if (!this.onRowEditorValidator(baseProduct)) {
            this.growl.show({severity: 'error', summary: 'Error', detail: 'Price inputs are invalid.'});
            return;
        }

        // update BigCommerce market
        let bigCommercePrice = null;
        if(baseProduct.vendHQPrice){
            bigCommercePrice = baseProduct.vendHQPrice;
        } else if(baseProduct.bigCommercePrice){
            bigCommercePrice = baseProduct.bigCommercePrice;
        } else if(baseProduct.bigCommerceFSPrice){
            bigCommercePrice = baseProduct.bigCommerceFSPrice;
        }

        if(bigCommercePrice !== null && !isNaN(parseFloat(bigCommercePrice))) {
            let priceParameters = {
                "bigCommerceRetailPrice" : bigCommercePrice,
                "marketPlace" : "BigCommerce"
            };
            this.props.commitPriceChange(baseProduct.sku, priceParameters);
            //console.log("commit price for bigcommerce " + bigCommercePrice);
        }

        // update Amazon market
        let amazonPrice = null;
        if(baseProduct.amazonCAPrice){
            amazonPrice = baseProduct.amazonCAPrice;
        }
        if(amazonPrice !== null && !isNaN(parseFloat(amazonPrice))) {
            let priceParameters = {
                "amazonPrice": amazonPrice,
                "marketPlace": "Amazon"
            };
            this.props.commitPriceChange(baseProduct.sku, priceParameters);
            //console.log("commit price for amazon");
        }
        
        // delete from modifiedProducts
        delete this.modifiedProducts[event.data.sku];

        this.growl.show({severity: 'success', summary: 'Success', detail: 'Price change saved.'});
    }

    onRowEditCancel(event) {
        let baseProduct = this.modifiedProducts[event.data.sku];
        this.props.updateBaseProductPrice(baseProduct);
        delete this.modifiedProducts[event.data.sku];
    }

    priceFieldRender(rowData, field){
        if(rowData[field])
            return rowData[field];
        return "Not Available";
    }

    render() {
        let header = <div className="p-clearfix" style={{lineHeight:'1.87em'}}>Product List - Bulk Edit Mode</div>;

        return (
            <div>
                <Growl ref={(el) => this.growl = el} />
                <div className="content-section implementation">
                    <DataTable value={this.props.productList} editMode="row"
                               paginator={true} rows={10}  header={header}
                               paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                               currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
                               rowEditorValidator={this.onRowEditorValidator}
                               onRowEditInit={this.onRowEditInit}
                               onRowEditSave={this.onRowEditSave}
                               onRowEditCancel={this.onRowEditCancel}>

                        <Column field="sku" header="Product SKU" sortable={true} filter={true} filterPlaceholder="search sku" filterMatchMode="contains" />
                        <Column field="name" header="Product Name" sortable={true} filter={true} filterPlaceholder="search product" filterMatchMode="contains" />

                        <Column body={(rowData) => this.priceFieldRender(rowData, 'vendHQPrice')} header="VendHQ Price"
                                editor={(props) => this.editorForRowEditing(props, 'vendHQPrice')} style={{height: '3.5em'}}/>
                        <Column body={(rowData) => this.priceFieldRender(rowData, 'bigCommercePrice')} header="BC Price"
                                editor={(props) => this.editorForRowEditing(props, 'bigCommercePrice')} style={{height: '3.5em'}}/>
                        <Column body={(rowData) => this.priceFieldRender(rowData, 'bigCommerceFSPrice')} header="BC-FS Price" 
                                editor={(props) => this.editorForRowEditing(props, 'bigCommerceFSPrice')} style={{height: '3.5em'}}/>
                        <Column body={(rowData) => this.priceFieldRender(rowData, 'amazonCAPrice')} header="Amazon CA Price"
                                editor={(props) => this.editorForRowEditing(props, 'amazonCAPrice')} style={{height: '3.5em'}}/>
                        <Column rowEditor={true} style={{'width': '70px', 'textAlign': 'center'}}></Column>
                    </DataTable>

                    <ProductDetailDialog visibleProperty={this.state.displayDialog}
                                         onHideEvent={() => this.setState({displayDialog: false})} productSku={null}
                                         setGrowlMessage={(messages) => this.setGrowlMessage(messages)}
                    />
                </div>
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        productList: state.product.productList
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getProductList: () => dispatch(getProductList()),
        updateBaseProductPrice: (baseOrder) => dispatch(updateBaseProductPrice(baseOrder)),
        commitPriceChange: (productSku, propertyChanges) => dispatch(commitPriceChange(productSku, propertyChanges, true))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(ListProductsBulkEdit);