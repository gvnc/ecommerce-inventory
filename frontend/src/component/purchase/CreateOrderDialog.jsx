import React, { Component } from 'react'
import {connect} from "react-redux";
import {Dialog} from "primereact/dialog";
import {InputText} from "primereact/inputtext";
import {Button} from "primereact/button";
import {createPurchaseOrder} from "../../store/actions/purchaseActions";

class CreateOrderDialog extends Component {

    constructor() {
        super();
        this.hideDialog = this.hideDialog.bind(this);
        this.onSave = this.onSave.bind(this);
        this.resetInputs = this.resetInputs.bind(this);
        this.successHandler = this.successHandler.bind(this);
        this.errorHandler = this.errorHandler.bind(this);
        this.getTodateAsStr = this.getTodateAsStr.bind(this);
        let todateAsStr = this.getTodateAsStr();
        this.state = {
            supplier: "",
            createDate: todateAsStr,
            createdBy:"Garner Lupo"
        }
    }

    resetInputs(){
        let todateAsStr = this.getTodateAsStr();
        this.setState({
            supplier: "",
            createDate: todateAsStr,
            createdBy:"Garner Lupo"
        });
    }

    onSave(){
        let purchaseOrder = {
            createdBy: this.state.createdBy,
            supplier: this.state.supplier,
        }
        this.props.createPurchaseOrder(purchaseOrder, this.successHandler, this.errorHandler);
    }

    errorHandler(){
        console.log("error handler ");
    }

    successHandler(){
        this.resetInputs();
        this.props.createOrderSuccessful();
    }

    hideDialog(){
        this.props.onHideEvent();
        this.resetInputs();
    }

    getTodateAsStr(){
        return "13/05/2020";
    }

    render() {
        console.log("render dialog");

        let dialogFooter = <div className="ui-dialog-buttonpane p-clearfix">
            <Button label="Create" icon="pi pi-check" onClick={this.onSave}/>
            <Button label="Cancel" icon="pi pi-times" onClick={this.hideDialog}/>
        </div>;

        return (
            <Dialog visible={this.props.visibleProperty} header="Create a purchase order" modal={true}
                    footer={dialogFooter} onHide={this.hideDialog} style={{width:'500px'}} >
                <div className="p-grid p-fluid">
                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Create Date</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {this.state.createDate}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Created By</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        <InputText id="createdBy" onChange={(e) => this.setState({createdBy: e.target.value}) }
                                   value={this.state.createdBy} />
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label htmlFor="price">Supplier</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        <InputText id="supplier" onChange={(e) => this.setState({supplier: e.target.value}) }
                                   value={this.state.supplier} />
                    </div>
                </div>
            </Dialog>
        )
    }
}

const mapStateToProps = state => {
    return {
    };
};

const mapDispatchToProps = dispatch => {
    return {
        createPurchaseOrder: (purchaseOrder,successHandler, errorHandler) => dispatch(createPurchaseOrder(purchaseOrder,successHandler, errorHandler))
    };
};


export default connect(mapStateToProps, mapDispatchToProps)(CreateOrderDialog);