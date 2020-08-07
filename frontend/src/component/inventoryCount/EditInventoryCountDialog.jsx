import React, { Component } from 'react'
import {connect} from "react-redux";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {InputText} from "primereact/inputtext";
import {Checkbox} from 'primereact/checkbox';
import ConfirmationDialog from "../ConfirmationDialog";
import {RadioButton} from "primereact/radiobutton";
import { updateSelectedInventoryCount, saveInventoryCount, startInventoryCount} from "../../store/actions/inventoryCountActions";
import {Growl} from "primereact/growl";
import ProductSelectComponent from "./ProductSelectComponent";

class EditInventoryCountDialog extends Component {

    constructor() {
        super();

        this.hideDialog = this.hideDialog.bind(this);
        this.resetInputs = this.resetInputs.bind(this);
        this.save = this.save.bind(this);
        this.inactiveProductsCheckEvent = this.inactiveProductsCheckEvent.bind(this);
        this.partialCountRadioEvent = this.partialCountRadioEvent.bind(this);
        this.saveSuccessHandler = this.saveSuccessHandler.bind(this);
        this.saveErrorHandler = this.saveErrorHandler.bind(this);
        this.start = this.start.bind(this);
        this.startSuccessHandler = this.startSuccessHandler.bind(this);

        this.state = {

        }
    }

    hideDialog(){
        this.props.onHideEvent();
        this.resetInputs();
        //this.props.setPurchaseOrderById(null);
    }

    resetInputs(){
     /*   this.setState({

        });

      */
    }

    inactiveProductsCheckEvent(e){
        this.props.updateSelectedInventoryCount("includeInactive", e.checked);
    }

    partialCountRadioEvent(e){
        this.props.updateSelectedInventoryCount("partialCount", e.value);
    }

    save(){
        let inventoryCount = this.props.inventoryCount;
        let inventoryCountProducts = this.props.inventoryCountProducts;
        this.props.saveInventoryCount(inventoryCount, inventoryCountProducts, this.saveSuccessHandler, this.saveErrorHandler);
    }

    saveSuccessHandler(){
        this.growl.show({severity: 'success', summary: 'Success', detail: 'Inventory count saved.'});
    }

    saveErrorHandler(){
        this.growl.show({severity: 'error', summary: 'Error', detail: 'Failed to save inventory count.'});
    }

    start(){
        this.props.startInventoryCount(this.props.inventoryCount.id, this.startSuccessHandler);
    }

    startSuccessHandler(){
        this.props.history.push("/inventoryCountInProgress/" + this.props.inventoryCount.id);
    }

    render() {
        let dialogFooter =  <div className="ui-dialog-buttonpane p-clearfix">
            <Button label="Save" icon="pi pi-check" onClick={this.save}/>
            <Button label="Start" icon="pi pi-check" onClick={this.start}/>
        </div>;

        //console.log("of " + JSON.stringify(this.props.inventoryCount));

        return (
            <Dialog visible={this.props.visibleProperty} modal={true} maximized={true}
                    footer={dialogFooter} onHide={this.hideDialog} showHeader={true} >
                <Growl ref={(el) => this.growl = el} />
                {
                    this.props.inventoryCount &&

                    <div className="p-grid p-fluid">
                        <div className="p-col-2 productProperty" style={{padding: '.75em'}}><label>Name</label></div>
                        <div className="p-col-4" style={{padding: '.75em'}}>
                            <InputText id="name" onChange={e => this.props.updateSelectedInventoryCount("name", e.target.value)}
                                       value={this.props.inventoryCount.name}/>
                        </div>
                        <div className="p-col-6" style={{padding: '.75em'}}>
                            <Checkbox inputId="includeInactiveCheck" onChange={e => this.inactiveProductsCheckEvent(e)}
                                      checked={this.props.inventoryCount.includeInactive}></Checkbox>
                            <label htmlFor="includeInactiveCheck" className="p-checkbox-label">include inactive
                                products</label>
                        </div>
                        <div className="p-col-3">
                            <RadioButton inputId="countType1" name="countType" value={false}
                                         onChange={e => this.partialCountRadioEvent(e)}
                                         checked={!this.props.inventoryCount.partialCount}/>
                            <label htmlFor="countType1" className="p-radiobutton-label">Full Count</label>
                        </div>
                        <div className="p-col-3">
                            <RadioButton inputId="countType2" name="countType" value={true}
                                         onChange={e => this.partialCountRadioEvent(e)}
                                         checked={this.props.inventoryCount.partialCount}/>
                            <label htmlFor="countType2" className="p-radiobutton-label">Partial Count</label>
                        </div>
                        <div className="p-col-6"></div>
                        <div className="p-col-12">
                            <ProductSelectComponent partialCount={this.props.inventoryCount.partialCount} />
                        </div>
                    </div>
                }
            </Dialog>
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
        updateSelectedInventoryCount: (propertyName, propertyValue) => dispatch(updateSelectedInventoryCount(propertyName, propertyValue)),
        saveInventoryCount: (inventoryCount, productList, successHandler, errorHandler) => dispatch(saveInventoryCount(inventoryCount, productList, successHandler, errorHandler)),
        startInventoryCount: (inventoryCountId, successHandler) => dispatch(startInventoryCount(inventoryCountId, successHandler))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(EditInventoryCountDialog);