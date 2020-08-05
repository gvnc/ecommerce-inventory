import React, { Component } from 'react'
import {connect} from "react-redux";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {DataTable} from "primereact/datatable";
import {InputText} from "primereact/inputtext";
import {Checkbox} from 'primereact/checkbox';
import ConfirmationDialog from "../ConfirmationDialog";
import {RadioButton} from "primereact/radiobutton";
import { updateSelectedInventoryCount } from "../../store/actions/inventoryCountActions";

class EditInventoryCountDialog extends Component {

    constructor() {
        super();

        this.hideDialog = this.hideDialog.bind(this);
        this.resetInputs = this.resetInputs.bind(this);
        this.saveInventoryCount = this.saveInventoryCount.bind(this);
        this.inactiveProductsCheckEvent = this.inactiveProductsCheckEvent.bind(this);
        this.partialCountRadioEvent = this.partialCountRadioEvent.bind(this);

        this.state = {
            displayProductSelect: false,
            checked: false,
            receiveList: [],
            displayPODeleteConfirmation: false,
            displayPOCancelConfirmation: false,
            receiveButtonEnabled: true
        }
    }

    hideDialog(){
        this.props.onHideEvent();
        //this.resetInputs();
        //this.props.setPurchaseOrderById(null);
    }

    resetInputs(){
        this.setState({
            displayProductSelect: false,
            checked: false,
            receiveList: [],
            displayPODeleteConfirmation: false,
            displayPOCancelConfirmation: false,
            receiveButtonEnabled: true
        });
    }

    inactiveProductsCheckEvent(e){
        this.props.updateSelectedInventoryCount("includeInactive", e.checked);
    }

    partialCountRadioEvent(e){
        this.props.updateSelectedInventoryCount("partialCount", e.value);
    }

    saveInventoryCount(){

    }

    render() {
        let dialogFooter =  <div className="ui-dialog-buttonpane p-clearfix">
            <Button label="Save" icon="pi pi-check" onClick={this.saveInventoryCount}/>
            <Button label="Start" icon="pi pi-check" onClick={this.saveInventoryCount}/>
        </div>;

        //console.log("of " + JSON.stringify(this.props.inventoryCount));

        return (

            <Dialog visible={this.props.visibleProperty} modal={true} style={{width:'800px'}}
                    footer={dialogFooter} onHide={this.hideDialog} showHeader={false} >
                {
                    this.props.inventoryCount &&

                    <div className="p-grid p-fluid">
                        <div className="p-col-3 productProperty" style={{padding: '.75em'}}><label>Name</label></div>
                        <div className="p-col-3" style={{padding: '.75em'}}>
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
                    </div>
                }
            </Dialog>
        )
    }
}

const mapStateToProps = state => {
    return {
        inventoryCount: state.inventoryCount.selectedInventoryCount,
        inventoryCountProducts: state.purchase.selectedInventoryCountProducts
    };
};

const mapDispatchToProps = dispatch => {
    return {
        updateSelectedInventoryCount: (propertyName, propertyValue) => dispatch(updateSelectedInventoryCount(propertyName, propertyValue))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(EditInventoryCountDialog);