import React, { Component } from 'react'
import {ToggleButton} from 'primereact/togglebutton';
import {Toolbar} from 'primereact/toolbar';
import ListProductsBulkEdit from "./ListProductsBulkEdit";
import ListProductsManualEdit from "./ListProductsManualEdit";

class ListProducts extends Component {

    constructor() {
        super();
        this.state = {
            isManualEditChecked: false
        };
    }

    render() {
        /*
        let editMode = "Bulk";
        if(this.state.isManualEditChecked === true){
            editMode = "Manual";
        }
        */
        return (
            <div>
                <div className="content-section implementation">
                    <Toolbar>
                        <div className="p-toolbar-group-left">
                            <div className="p-clearfix" style={{lineHeight:'1.87em', fontSize:18}}>All products are listed.</div>
                        </div>
                        <div className="p-toolbar-group-right">
                            <ToggleButton style={{width:'250px', marginRight:'.25em'}} checked={this.state.isManualEditChecked}
                                          onChange={(e) => this.setState({isManualEditChecked: e.value})}
                                          onLabel="Switch to Bulk Mode" offLabel="Switch to Manual Mode" />
                        </div>
                    </Toolbar>
                    {
                        this.state.isManualEditChecked === false && <ListProductsBulkEdit />
                    }
                    {
                        this.state.isManualEditChecked === true && <ListProductsManualEdit />
                    }
                </div>
            </div>
        )
    }
}
export default ListProducts;