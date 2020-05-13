import React, { Component } from 'react'
import {connect} from "react-redux";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {getProductList} from "../../store/actions/productActions";

class ProductSelectDialog extends Component {

    constructor() {
        super();
        this.hideDialog = this.hideDialog.bind(this);
        this.onProductSelect = this.onProductSelect.bind(this);
        this.resetInputs = this.resetInputs.bind(this);
        this.addButtonBody = this.addButtonBody.bind(this);

        this.state = {
            selectedProducts: []
        }
    }

    componentDidMount() {
        if (this.props.productList.length === 0) {
            this.props.getProductList();
        }
    }

    resetInputs(){
        this.setState({
            selectedProducts: []
        });
    }

    onProductSelect(){

    }

    hideDialog(){
        this.props.onHideEvent();
        this.resetInputs();
    }

    addButtonBody(rowData) {
        return (
            <Button type="button" icon="pi pi-plus" className="p-button-secondary" onClick={() => console.log(rowData.sku)}></Button>
        );
    }

    render() {
        console.log("off render:" + this.props.productList.length);
        let dialogFooter =  <div className="ui-dialog-buttonpane p-clearfix">
                                <Button label="Add Selected Products" onClick={this.hideDialog}/>
                            </div>;

        return (
            <Dialog visible={this.props.visibleProperty} header="select from the products" modal={true}
                    footer={dialogFooter} onHide={this.hideDialog} style={{width:'800px'}} >
                <DataTable value={this.props.productList} paginator={true} rows={5} style={{height:'370px'}}
                           selection={this.state.selectedProducts}
                           onSelectionChange={e => this.setState({selectedProducts: e.value})}
                           onRowSelect={this.onProductSelect}
                           paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                           currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
                           headerStyle={{display:'none'}}>
                    <Column selectionMode="multiple" style={{width:'3em'}}/>
                    <Column field="sku" header="Product SKU" filter={true} filterPlaceholder="search sku" filterMatchMode="contains" style={{width:'170px'}} />
                    <Column field="name" header="Product Name" filter={true} filterPlaceholder="search product" filterMatchMode="contains" />
                    <Column body={this.addButtonBody} headerStyle={{width: '4em', textAlign: 'center'}} bodyStyle={{textAlign: 'center', overflow: 'visible'}}   />
                </DataTable>
            </Dialog>
        )
    }
}

const mapStateToProps = state => {
    return {
        productsRequested: state.product.productsRequested,
        productList: state.product.productList
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getProductList: () => dispatch(getProductList())
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(ProductSelectDialog);