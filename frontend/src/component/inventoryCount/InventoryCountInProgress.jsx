import React, { Component } from 'react'
import { connect } from "react-redux";
import { getInventoryCountById } from "../../store/actions/inventoryCountActions";
import {Card} from 'primereact/card';
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Button} from "primereact/button";
import {InputText} from "primereact/inputtext";
import {Fieldset} from "primereact/fieldset";

class InventoryCountInProgress extends Component {

    constructor() {
        super();
        this.state= {
            selectedProduct : null,
            countValue: ""
        };
        this.countProduct = this.countProduct.bind(this);
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
        let selectedProductInstance = this.state.selectedProduct;
        selectedProductInstance.count = this.state.countValue;
        selectedProductInstance.counted = true;

        console.log("button clicked !! ");
    }

    render() {
        return (
                this.props.inventoryCount &&
                <div>
                    <Fieldset legend="Inventory Count" style={{fontSize:18}}>
                        Name : {this.props.inventoryCount.name} | Created At : {this.props.inventoryCount.createDate} | Status : {this.props.inventoryCount.status}
                    </Fieldset>
                    {
                        this.state.selectedProduct === null &&
                        <Card style={{height:'85px', width:'100%', marginTop:'2px', textAlign:'center', fontSize:18}}>
                            Select a product to count.
                        </Card>
                    }
                    {
                        this.state.selectedProduct !== null &&
                        <Card style={{height:'85px', width:'100%', marginTop:'2px', paddingTop: '20px'}}>
                            <div className="p-grid p-fluid">
                                <div className="p-col-5">
                                    <span className="p-float-label">
                                        <InputText id="nameInput" value={this.state.selectedProduct.name} readonly={true} />
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
                    <div className="content-section implementation" style={{marginTop:'2px'}}>
                        <DataTable value={this.props.inventoryCountProducts} paginator={true} rows={10} selectionMode="single"
                                   selection={this.state.selectedProduct} onSelectionChange={e => this.setState({selectedProduct: e.value, countValue:e.value.count})}
                                   paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                   currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries" >
                            <Column field="sku" header="Product SKU" sortable={true} filter={true} filterPlaceholder="search sku" filterMatchMode="contains" />
                            <Column field="name" header="Product Name" sortable={true} filter={true} filterPlaceholder="search product" filterMatchMode="contains" />
                            <Column header="VendHQ Quantity" field="vendhqQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                            <Column header="BC Quantity" field="bigcommerceQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                            <Column header="BC-FS Quantity" field="bigcommerceFSQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                            <Column header="Amazon CA Quantity" field="amazonCAQuantity" style={{height: '3.5em', 'textAlign': 'center'}}/>
                            <Column header="Counted Quantity" field="count" style={{height: '3.5em', 'textAlign': 'center'}}/>
                        </DataTable>
                    </div>
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
        getInventoryCountById: (id) => dispatch(getInventoryCountById(id))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(InventoryCountInProgress);