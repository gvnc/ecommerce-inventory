import React, { Component } from 'react'
import { connect } from "react-redux";
import { getInventoryCountById, saveInventoryCountProduct, abandonInventoryCount, reviewInventoryCount} from "../../store/actions/inventoryCountActions";
import {Card} from 'primereact/card';
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Button} from "primereact/button";
import {InputText} from "primereact/inputtext";
import {Fieldset} from "primereact/fieldset";
import {Growl} from "primereact/growl";
import {TabView,TabPanel} from 'primereact/tabview';
import {Toolbar} from "primereact/toolbar";
import ConfirmationDialog from "../ConfirmationDialog";

class InventoryCountInProgress extends Component {

    constructor() {
        super();
        this.state= {
            selectedProduct : null,
            countValue: "",
            activeIndex: 0,
            displayAbandonConfirmation: false
        };
        this.countProduct = this.countProduct.bind(this);
        this.abandonSuccessHandler = this.abandonSuccessHandler.bind(this);
        this.reviewSuccessHandler = this.reviewSuccessHandler.bind(this);
    }

    componentDidMount() {
        let inventoryCountId = this.props.match.params.inventoryCountId;
        if(this.props.inventoryCount !== undefined && this.props.inventoryCount !== null && this.props.inventoryCount.id === inventoryCountId){
            // if same id with props selected, do nothing
        } else {
            this.props.getInventoryCountById(inventoryCountId);
        }
    }

    countProduct(){
        let selectedProduct = this.state.selectedProduct;
        if(selectedProduct.count && selectedProduct.count !== ""){
            selectedProduct.count = Number(selectedProduct.count) + Number(this.state.countValue);
        } else{
            selectedProduct.count = Number(this.state.countValue);
        }
        selectedProduct.counted = true;

        let matched = true;
        let anyProductHit = false;

        if(selectedProduct.vendhqQuantity !== null){
            anyProductHit = true;
            if(this.forceToString(selectedProduct.vendhqQuantity) !== this.state.countValue.toString()){
                matched = false;
            }
        }
        if(selectedProduct.squareQuantity !== null){
            anyProductHit = true;
            if(this.forceToString(selectedProduct.squareQuantity) !== this.state.countValue.toString()){
                matched = false;
            }
        }
        if(selectedProduct.bigcommerceQuantity !== null){
            anyProductHit = true;
            if(this.forceToString(selectedProduct.bigcommerceQuantity) !== this.state.countValue.toString()){
                matched = false;
            }
        }
        if(selectedProduct.bigcommerceFSQuantity !== null){
            anyProductHit = true;
            if(this.forceToString(selectedProduct.bigcommerceFSQuantity) !== this.state.countValue.toString()){
                matched = false;
            }
        }
        if(selectedProduct.amazonCAQuantity !== null){
            anyProductHit = true;
            if(this.forceToString(selectedProduct.amazonCAQuantity) !== this.state.countValue){
                matched = false;
            }
        }
        if(anyProductHit === true){
            selectedProduct.matched = matched;
        } else {
            selectedProduct.matched = false;
        }

        // reset countvalue
        this.setState({countValue:"0"})

        // save count product
        this.props.saveInventoryCountProduct(selectedProduct);
        this.growl.show({severity: 'success', summary: 'Success', detail: 'Count saved.'});
    }

    forceToString(value){
        if(value !== null && value !== undefined)
            return value.toString();
        return "";
    }

    abandonSuccessHandler(){
        this.growl.show({severity: 'success', summary: 'Success', detail: 'Inventory count abandoned.'});
    }

    reviewSuccessHandler(){
        this.props.history.push("/inventoryCountReview/" + this.props.inventoryCount.id);
    }

    render() {

        let countedProducts = this.props.inventoryCountProducts.filter(p => p.counted === true);
        let uncountedProducts = this.props.inventoryCountProducts.filter(p => p.counted === false);

        return (
                this.props.inventoryCount &&
                <div>
                    <Growl ref={(el) => this.growl = el} />
                    <Fieldset legend="Inventory Count" style={{fontSize:18}}>
                        Name : {this.props.inventoryCount.name} | Created At : {this.props.inventoryCount.createDate} | Status : {this.props.inventoryCount.status}
                    </Fieldset>
                    {
                        this.props.inventoryCount.status !=="STARTED" &&
                        <Card style={{height:'85px', width:'100%', marginTop:'2px', textAlign:'center', fontSize:18}}>
                            This count is in {this.props.inventoryCount.status} state. You can not count products anymore.
                        </Card>
                    }
                    {
                        this.props.inventoryCount.status ==="STARTED" && this.state.selectedProduct === null &&
                        <Card style={{height:'85px', width:'100%', marginTop:'2px', textAlign:'center', fontSize:18}}>
                            Select a product to count.
                        </Card>
                    }
                    {
                        this.props.inventoryCount.status ==="STARTED" && this.state.selectedProduct !== null &&
                        <Card style={{height:'85px', width:'100%', marginTop:'2px', paddingTop: '20px'}}>
                            <div className="p-grid p-fluid">
                                <div className="p-col-5">
                                    <span className="p-float-label">
                                        <InputText id="nameInput" value={this.state.selectedProduct.name} readOnly={true} />
                                        <label htmlFor="nameInput">Product Name</label>
                                    </span>
                                </div>
                                <div className="p-col-1">
                                    <span className="p-float-label">
                                        <InputText id="countInput" value={this.state.countValue} keyfilter="int"
                                                   onChange={e => this.setState({countValue: e.target.value})}/>
                                        <label htmlFor="countInput">Quantity</label>
                                    </span>
                                </div>
                                <div className="p-col-1">
                                    <Button className="p-button-success" label="COUNT" onClick={this.countProduct} />
                                </div>
                            </div>
                        </Card>
                    }
                    <div style={{marginTop:'2px'}}>
                        <TabView activeIndex={this.state.activeIndex} renderActiveOnly={true}
                                 onTabChange={(e) => this.setState({activeIndex: e.index})}>
                            <TabPanel header="All Products">
                                <DataTable value={this.props.inventoryCountProducts} paginator={true} rows={10} selectionMode="single"
                                           selection={this.state.selectedProduct} onSelectionChange={e => this.setState({selectedProduct: e.value, countValue:"0"})}
                                           paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                           currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries" >
                                    <Column field="sku" header="Product SKU" sortable={true} filter={true} filterPlaceholder="search sku" filterMatchMode="contains" />
                                    <Column field="name" header="Product Name" sortable={true} filter={true} filterPlaceholder="search product" filterMatchMode="contains" />
                                    <Column header="VendHQ Quantity" field="vendhqQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    {/* // remove comment out to enable square
                                    <Column header="SquareUp Quantity" field="squareQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/> */}
                                    <Column header="BC Quantity" field="bigcommerceQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="BC-FS Quantity" field="bigcommerceFSQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="Amazon CA Quantity" field="amazonCAQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="Counted Quantity" field="count" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                </DataTable>
                            </TabPanel>
                            <TabPanel header="Uncounted">
                                <DataTable value={uncountedProducts} paginator={true} rows={10} selectionMode="single"
                                           selection={this.state.selectedProduct} onSelectionChange={e => this.setState({selectedProduct: e.value, countValue:"0"})}
                                           paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                           currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries" >
                                    <Column field="sku" header="Product SKU" sortable={true} filter={true} filterPlaceholder="search sku" filterMatchMode="contains" />
                                    <Column field="name" header="Product Name" sortable={true} filter={true} filterPlaceholder="search product" filterMatchMode="contains" />
                                    <Column header="VendHQ Quantity" field="vendhqQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    {/* // remove comment out to enable square
                                    <Column header="SquareUp Quantity" field="squareQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/> */}
                                    <Column header="BC Quantity" field="bigcommerceQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="BC-FS Quantity" field="bigcommerceFSQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="Amazon CA Quantity" field="amazonCAQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="Counted Quantity" field="count" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                </DataTable>
                            </TabPanel>
                            <TabPanel header="Counted">
                                <DataTable value={countedProducts} paginator={true} rows={10} selectionMode="single"
                                           selection={this.state.selectedProduct} onSelectionChange={e => this.setState({selectedProduct: e.value, countValue:"0"})}
                                           paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                           currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries" >
                                    <Column field="sku" header="Product SKU" sortable={true} filter={true} filterPlaceholder="search sku" filterMatchMode="contains" />
                                    <Column field="name" header="Product Name" sortable={true} filter={true} filterPlaceholder="search product" filterMatchMode="contains" />
                                    <Column header="VendHQ Quantity" field="vendhqQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    {/* // remove comment out to enable square
                                    <Column header="SquareUp Quantity" field="squareQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/> */}
                                    <Column header="BC Quantity" field="bigcommerceQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="BC-FS Quantity" field="bigcommerceFSQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="Amazon CA Quantity" field="amazonCAQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                    <Column header="Counted Quantity" field="count" style={{height: '3.5em', 'textAlign': 'center'}}/>
                                </DataTable>
                            </TabPanel>
                        </TabView>
                    </div>
                    {
                        this.props.inventoryCount.status === "STARTED" &&
                        <Toolbar>
                            <div className="p-toolbar-group-left">
                                <Button label="Abandon Count" icon="pi pi-sign-out" style={{marginRight:'.25em'}}
                                        onClick={() => this.setState({displayAbandonConfirmation: true})} />
                            </div>
                            <div className="p-toolbar-group-right">
                                <Button label="Review Count" icon="pi pi-briefcase" style={{marginRight:'.25em'}}
                                        onClick={() => this.props.reviewInventoryCount(this.props.inventoryCount.id, this.reviewSuccessHandler)}  />
                            </div>
                        </Toolbar>
                    }

                    <ConfirmationDialog  visibleProperty={this.state.displayAbandonConfirmation}
                                         noHandler={() => this.setState({displayAbandonConfirmation: false})}
                                         yesHandler={() => {
                                             this.setState({displayAbandonConfirmation: false});
                                             this.props.abandonInventoryCount(this.props.inventoryCount.id, this.abandonSuccessHandler)
                                         }}
                                         message="You will no longer be able to edit this count. Do you confirm to abandon ?" />
                </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        inventoryCount: state.inventoryCount.selectedInventoryCount,
        inventoryCountProducts: state.inventoryCount.selectedInventoryCountProducts
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getInventoryCountById: (id) => dispatch(getInventoryCountById(id)),
        saveInventoryCountProduct: (inventoryCountProduct) => dispatch(saveInventoryCountProduct(inventoryCountProduct)),
        abandonInventoryCount: (inventoryCountId, successHandler) => dispatch(abandonInventoryCount(inventoryCountId, successHandler)),
        reviewInventoryCount: (inventoryCountId, successHandler) => dispatch(reviewInventoryCount(inventoryCountId, successHandler))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(InventoryCountInProgress);