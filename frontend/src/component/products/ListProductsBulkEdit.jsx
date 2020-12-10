import React, { Component } from 'react'
import { connect } from "react-redux";
import {
    commitPriceChange,
    getProductList,
    updateBaseProductPrice,
    updateInventory
} from "../../store/actions/productActions"
import {DataTable} from "primereact/datatable";
import {InputText} from "primereact/inputtext";
import {Column} from "primereact/column";
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
        this.marketPlaceRender = this.marketPlaceRender.bind(this);
        this.inventoryFieldRender = this.inventoryFieldRender.bind(this);
        this.renderSingleColumn = this.renderSingleColumn.bind(this);
        this.singleFieldRender = this.singleFieldRender.bind(this);
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
        if(baseProduct.squarePrice !== null)
            baseProduct.squarePrice = newValue;
    }

    syncAmazonPrice(baseProduct, newValue){
        if(baseProduct.amazonCAPrice !== null)
            baseProduct.amazonCAPrice = newValue;
    }

    syncInventoryForAll(baseProduct, newValue){
        if(baseProduct.bigCommerceInventory !== null)
            baseProduct.bigCommerceInventory = newValue;
        if(baseProduct.bigCommerceFSInventory !== null)
            baseProduct.bigCommerceFSInventory = newValue;
        if(baseProduct.vendHQInventory !== null)
            baseProduct.vendHQInventory = newValue;
        if(baseProduct.amazonCAInventory !== null)
            baseProduct.amazonCAInventory = newValue;
        if(baseProduct.squareInventory !== null)
            baseProduct.squareInventory = newValue;
    }

    /* Row Editing */
    onEditorValueChangeForRowEditing(baseProduct, field, value) {
        var floatRegexPattern = /^\d*(\.\d*)?$/;
        if(value === "" || floatRegexPattern.test(value)){
            if(field === "bigCommercePrice" || field === "bigCommerceFSPrice" || field === "vendHQPrice" || field === "squarePrice"){
                this.syncBigCommercePrice(baseProduct, value);
            } else if(field === "amazonCAPrice"){
                this.syncAmazonPrice(baseProduct, value);
            }
            this.props.updateBaseProductPrice(baseProduct);
        }
        var intRegexPattern = /^\d*?$/;
        if(value === "" || intRegexPattern.test(value)) {
            if (field === "bigCommerceInventory" || field === "bigCommerceFSInventory" || field === "vendHQInventory" || field === "amazonCAInventory" || field === "squareInventory") {
                this.syncInventoryForAll(baseProduct, value);
                this.props.updateBaseProductPrice(baseProduct);
            }
        }
    }

    editorForRowEditing(props, field) {
        if(field === "price"){
            return <div className="p-grid p-dir-col">
                {/* this.renderSingleColumn(props, "vendHQPrice")*/}
                { this.renderSingleColumn(props, "squarePrice")}
                { this.renderSingleColumn(props, "bigCommercePrice")}
                { this.renderSingleColumn(props, "bigCommerceFSPrice")}
                { this.renderSingleColumn(props, "amazonCAPrice")}
            </div>
        }
        if(field === "inventory"){
            return <div className="p-grid p-dir-col">
                {/* this.renderSingleColumn(props, "vendHQInventory")*/}
                { this.renderSingleColumn(props, "squareInventory")}
                { this.renderSingleColumn(props, "bigCommerceInventory")}
                { this.renderSingleColumn(props, "bigCommerceFSInventory")}
                { this.renderSingleColumn(props, "amazonCAInventory")}
            </div>
        }
        return <div/>;
    }

    renderSingleColumn(props, field) {
        return  <div className="p-col">
                {
                    props.rowData[field] === null ? NOT_AVAILABLE :
                        <InputText type="text" value={props.rowData[field]} style={componentCss.input}
                                   onChange={(e) => this.onEditorValueChangeForRowEditing(props.rowData, field, e.target.value)} />
                }
                </div>
    }

    isPriceValid(value){
        if(value !== null && (value === "" || value === "." || value.toString().substr(value.length-1) === ".")){
            return false;
        }
        return true;
    }

    isInventoryValid(value){
        if(value === ""){
            return false;
        }
        return true;
    }

    onRowEditorValidator(rowData) {
        let isValid = true;
        isValid = isValid && this.isPriceValid(rowData["vendHQPrice"]);
        isValid = isValid && this.isPriceValid(rowData["squarePrice"]);
        isValid = isValid && this.isPriceValid(rowData["bigCommercePrice"]);
        isValid = isValid && this.isPriceValid(rowData["bigCommerceFSPrice"]);
        isValid = isValid && this.isPriceValid(rowData["amazonCAPrice"]);

        isValid = isValid && this.isInventoryValid(rowData["vendHQInventory"]);
        isValid = isValid && this.isInventoryValid(rowData["squareInventory"]);
        isValid = isValid && this.isInventoryValid(rowData["bigCommerceInventory"]);
        isValid = isValid && this.isInventoryValid(rowData["bigCommerceFSInventory"]);
        isValid = isValid && this.isInventoryValid(rowData["amazonCAInventory"]);

        return isValid;
    }

    onRowEditInit(event) {
        this.modifiedProducts[event.data.sku] = {...event.data};
    }

    onRowEditSave(event) {
        let baseProduct = event.data;

        // validate fields
        if (!this.onRowEditorValidator(baseProduct)) {
            this.growl.show({severity: 'error', summary: 'Error', detail: 'Price/Inventory inputs are invalid.'});
            return;
        }

        // update BigCommerce market
        let bigCommercePrice = null;
        if(baseProduct.vendHQPrice){
            bigCommercePrice = baseProduct.vendHQPrice;
        } else if(baseProduct.squarePrice){
            bigCommercePrice = baseProduct.squarePrice;
        } else if(baseProduct.bigCommercePrice){
            bigCommercePrice = baseProduct.bigCommercePrice;
        } else if(baseProduct.bigCommerceFSPrice){
            bigCommercePrice = baseProduct.bigCommerceFSPrice;
        }

        if(bigCommercePrice !== null && !isNaN(parseFloat(bigCommercePrice))) {
            let priceParameters = {
                "bigCommercePrice" : bigCommercePrice,
                "bigCommerceRetailPrice" : bigCommercePrice,
                "marketPlace" : "BigCommerce"
            };
            this.props.commitPriceChange(baseProduct.sku, priceParameters);
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
        }

        // update inventory
        let inventoryLevel = null;
        if(baseProduct.vendHQInventory){
            inventoryLevel = baseProduct.vendHQInventory;
        } else if(baseProduct.squareInventory){
            inventoryLevel = baseProduct.squareInventory;
        } else if(baseProduct.bigCommerceInventory){
            inventoryLevel = baseProduct.bigCommerceInventory;
        } else if(baseProduct.bigCommerceFSInventory){
            inventoryLevel = baseProduct.bigCommerceFSInventory;
        } else if(baseProduct.amazonCAInventory){
            inventoryLevel = baseProduct.amazonCAInventory;
        }

        if(inventoryLevel !== null){
            this.props.updateInventory(baseProduct.sku, inventoryLevel);
        }
        
        // delete from modifiedProducts
        delete this.modifiedProducts[event.data.sku];

        this.growl.show({severity: 'success', summary: 'Success', detail: 'Changes saved for sku ' + event.data.sku + '.'});
    }

    onRowEditCancel(event) {
        let baseProduct = this.modifiedProducts[event.data.sku];
        this.props.updateBaseProductPrice(baseProduct);
        delete this.modifiedProducts[event.data.sku];
    }

    priceFieldRender(rowData){
        return  <div className="p-grid p-dir-col">
                    {/*this.singleFieldRender(rowData, "vendHQPrice")*/}
                    {this.singleFieldRender(rowData, "squarePrice")}
                    {this.singleFieldRender(rowData, "bigCommercePrice")}
                    {this.singleFieldRender(rowData, "bigCommerceFSPrice")}
                    {this.singleFieldRender(rowData, "amazonCAPrice")}
                </div>
    }

    inventoryFieldRender(rowData){
        return  <div className="p-grid p-dir-col">
                    {/*this.singleFieldRender(rowData, "vendHQInventory")*/}
                    {this.singleFieldRender(rowData, "squareInventory")}
                    {this.singleFieldRender(rowData, "bigCommerceInventory")}
                    {this.singleFieldRender(rowData, "bigCommerceFSInventory")}
                    {this.singleFieldRender(rowData, "amazonCAInventory")}
                </div>
    }

    singleFieldRender(rowData, field){
        return  <div className="p-col">
                    {
                        rowData[field] !== null ? rowData[field] : NOT_AVAILABLE
                    }
                </div>;
    }

    marketPlaceRender(){
        return  <div className="p-grid p-dir-col">
            {/*<div className="p-col">VendHQ</div>*/}
                    <div className="p-col">SquareUp</div>
                    <div className="p-col">BigComm.</div>
                    <div className="p-col">BigComm.FS</div>
                    <div className="p-col">Amazon CA</div>
                </div>
    }

    render() {
        return (
            <div>
                <Growl ref={(el) => this.growl = el} />
                <div className="content-section implementation">
                    <DataTable value={this.props.productList} editMode="row"
                               paginator={true} rows={5}
                               paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                               currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
                               rowEditorValidator={this.onRowEditorValidator}
                               onRowEditInit={this.onRowEditInit}
                               onRowEditSave={this.onRowEditSave}
                               onRowEditCancel={this.onRowEditCancel}>

                        <Column field="sku" header="Product SKU" sortable={true} filter={true} filterPlaceholder="search sku" filterMatchMode="contains"
                                style={componentCss.skuCol}  headerStyle={componentCss.skuCol} filterHeaderStyle={componentCss.skuCol} />

                        <Column field="name" header="Product Name" sortable={true} filter={true} filterPlaceholder="search product" filterMatchMode="contains"
                                style={componentCss.nameCol}  headerStyle={componentCss.nameCol} filterHeaderStyle={componentCss.nameCol} />

                        <Column body={this.marketPlaceRender} header="Market Place"
                                style={componentCss.priceCol} headerStyle={componentCss.priceCol} filterHeaderStyle={componentCss.priceCol} />

                        <Column body={(rowData) => this.priceFieldRender(rowData)} header="Price"
                                editor={(props) => this.editorForRowEditing(props, 'price')}
                                style={componentCss.priceCol}  headerStyle={componentCss.priceCol} filterHeaderStyle={componentCss.priceCol} />

                        <Column body={(rowData) => this.inventoryFieldRender(rowData)} header="Inventory"
                                editor={(props) => this.editorForRowEditing(props, 'inventory')}
                                style={componentCss.priceCol}  headerStyle={componentCss.priceCol} filterHeaderStyle={componentCss.priceCol} />

                        <Column rowEditor={true} style={componentCss.editCol}  headerStyle={componentCss.editCol} filterHeaderStyle={componentCss.editCol} ></Column>
                    </DataTable>
                </div>
            </div>
        )
    }
}

const componentCss={
    skuCol:{
        height:'3.5em',
        width:'150px'
    },
    nameCol:{
        height:'3.5em'
    },
    priceCol:{
        height:'3.5em',
        width:'125px'
    },
    editCol:{
        height:'3.5em',
        width:'70px',
        textAlign:'center'
    },
    input:{
        lineHeight: '15px'
    }
}

const NOT_AVAILABLE = " --- ";

const mapStateToProps = state => {
    return {
        productList: state.product.productList
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getProductList: () => dispatch(getProductList()),
        updateBaseProductPrice: (baseOrder) => dispatch(updateBaseProductPrice(baseOrder)),
        commitPriceChange: (productSku, propertyChanges) => dispatch(commitPriceChange(productSku, propertyChanges, true)),
        updateInventory: (productSku, inventoryChange) => dispatch(updateInventory(productSku, inventoryChange, true)),
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(ListProductsBulkEdit);