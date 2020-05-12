import React, { Component } from 'react'
import {InputText} from "primereact/inputtext";
import {Button} from "primereact/button";

class InventoryUpdate extends Component {

    render() {

        let updateButton = <Button label="Update Inventory" icon="pi pi-check" className="contentsMarginRight"
                                   onClick={this.props.updateInventoryEvent} />;
        if(this.props.updateInventoryInProgress === true)
            updateButton = <Button label="Update Inventory" icon="pi pi-check" className="contentsMarginRight"
                    onClick={this.props.updateInventoryEvent} disabled="disabled"/>;

        let saveButton = <Button label="Commit Price Change" icon="pi pi-check" onClick={this.props.save}/>
        if(this.props.savePriceInProgress === true)
            saveButton = <Button label="Commit Price Change" icon="pi pi-check" onClick={this.props.save}  disabled="disabled"/>

        return (
            <div className="ui-dialog-buttonpane p-clearfix" style={{padding:20}}>
                <label htmlFor="inventory" className="contentsMarginRight productProperty" >Iventory Level</label>
                <InputText id="inventory" className="contentsMarginRight" onChange={(e) => {this.props.updateProperty('inventoryLevel', e.target.value)}}
                           value={this.props.inventoryLevel} style={{width:'75px'}} keyfilter = "int" />
                {updateButton}
                {saveButton}
            </div>
        )
    }
}

export default InventoryUpdate;