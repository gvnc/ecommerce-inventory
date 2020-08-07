import React, { Component } from 'react'
import { connect } from "react-redux";
import { getInventoryCountById } from "../../store/actions/inventoryCountActions";

import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Button} from "primereact/button";

class InventoryCountInProgress extends Component {

    constructor() {
        super();
        this.state= { };
    }

    componentDidMount() {
        let inventoryCountId = this.props.match.params.inventoryCountId;
        // if same id with props selected, do nothing
        if(this.props.inventoryCount.id !== inventoryCountId){
            this.props.getInventoryCountById(inventoryCountId);
        }
    }

    render() {
        return (
            <p>{this.props.inventoryCount.name}</p>
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