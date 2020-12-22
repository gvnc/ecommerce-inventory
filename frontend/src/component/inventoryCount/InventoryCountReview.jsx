import React, { Component } from 'react'
import { connect } from "react-redux";
import { getInventoryCountById, updateInventoryCount, abandonInventoryCount} from "../../store/actions/inventoryCountActions";
import {Card} from 'primereact/card';
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Button} from "primereact/button";
import {Fieldset} from "primereact/fieldset";
import {Growl} from "primereact/growl";
import {TabView,TabPanel} from 'primereact/tabview';
import {Toolbar} from "primereact/toolbar";
import ConfirmationDialog from "../ConfirmationDialog";

class InventoryCountReview extends Component {

    constructor() {
        super();
        this.state= {
            selectedProduct : null,
            activeIndex: 0,
            displayUpdateConfirmation: false,
            displayAbandonConfirmation: false
        };
        this.updateSuccessHandler = this.updateSuccessHandler.bind(this);
        this.abandonSuccessHandler = this.abandonSuccessHandler.bind(this);
    }

    componentDidMount() {
        let inventoryCountId = this.props.match.params.inventoryCountId;
        if(this.props.inventoryCount !== undefined && this.props.inventoryCount !== null && this.props.inventoryCount.id === inventoryCountId){
            // if same id with props selected, do nothing
        } else {
            this.props.getInventoryCountById(inventoryCountId);
        }
    }

    abandonSuccessHandler(){
        this.growl.show({severity: 'success', summary: 'Success', detail: 'Inventory count abandoned.'});
    }

    updateSuccessHandler(){

    }

    render() {
        let countedProducts = this.props.inventoryCountProducts.filter(p => p.counted === true);
        let matchedProducts = this.props.inventoryCountProducts.filter(p => p.counted === true && p.matched === true);
        let unmatchedProducts = this.props.inventoryCountProducts.filter(p => p.counted === true && p.matched === false);

        return (
                this.props.inventoryCount &&
                <div>
                    <Growl ref={(el) => this.growl = el} />
                    <Fieldset legend="Inventory Count" style={{fontSize:18}}>
                        Name : {this.props.inventoryCount.name} | Created At : {this.props.inventoryCount.createDate} | Status : {this.props.inventoryCount.status}
                    </Fieldset>
                    {
                        this.props.inventoryCount.status !=="REVIEW" &&
                        <Card style={{height:'85px', width:'100%', marginTop:'2px', textAlign:'center', fontSize:18}}>
                            This count is in {this.props.inventoryCount.status} state. You are not allowed to review.
                        </Card>
                    }
                    {
                        this.props.inventoryCount.status ==="REVIEW" &&
                        <Card style={{height:'85px', width:'100%', marginTop:'2px', textAlign:'center', fontSize:18}}>
                            Counted products : {countedProducts.length} | Matched products : {matchedProducts.length} | Unmatched products : {unmatchedProducts.length}
                        </Card>
                    }
                    <div style={{marginTop:'2px'}}>
                        <TabView activeIndex={this.state.activeIndex} renderActiveOnly={true}
                                 onTabChange={(e) => this.setState({activeIndex: e.index})}>
                            <TabPanel header="Counted Products">
                                <DataTable value={countedProducts} paginator={true} rows={10}
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
                            <TabPanel header="Matched">
                                <DataTable value={matchedProducts} paginator={true} rows={10}
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
                            <TabPanel header="UnMatched">
                                <DataTable value={unmatchedProducts} paginator={true} rows={10}
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
                        this.props.inventoryCount.status === "REVIEW" &&
                        <Toolbar>
                            <div className="p-toolbar-group-left">
                                <Button label="Abandon Count" icon="pi pi-sign-out" style={{marginRight:'.25em'}}
                                        onClick={() => this.setState({displayAbandonConfirmation: true})} />
                            </div>
                            <div className="p-toolbar-group-right">
                                <Button label="Update Inventory" icon="pi pi-save" style={{marginRight:'.25em'}}
                                        onClick={() => this.setState({displayUpdateConfirmation: true})} />
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

                    <ConfirmationDialog visibleProperty={this.state.displayUpdateConfirmation}
                                         noHandler={() => this.setState({displayUpdateConfirmation: false})}
                                         yesHandler={() => {
                                             this.setState({displayUpdateConfirmation: false});
                                             //this.props.abandonInventoryCount(this.props.inventoryCount.id, this.abandonSuccessHandler)
                                         }}
                                         message="Do you confirm to update inventory in market places ?" />
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
        abandonInventoryCount: (inventoryCountId, successHandler) => dispatch(abandonInventoryCount(inventoryCountId, successHandler)),
        updateInventoryCount: (inventoryCountId, successHandler) => dispatch(updateInventoryCount(inventoryCountId, successHandler))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(InventoryCountReview);