import React, { Component } from 'react'
import {connect} from "react-redux";
import {Button} from "primereact/button";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {getProductList} from "../../store/actions/productActions";
import {addSelectedInventoryCountProducts, removeSelectedInventoryCountProducts} from "../../store/actions/inventoryCountActions"

class ProductSelectComponent extends Component {

    constructor() {
        super();
        this.onProductSelect = this.onProductSelect.bind(this);
        this.resetInputs = this.resetInputs.bind(this);

        this.rightClicked = this.rightClicked.bind(this);
        this.doubleRightClicked = this.doubleRightClicked.bind(this);
        this.leftClicked = this.leftClicked.bind(this);
        this.doubleLeftClicked = this.doubleLeftClicked.bind(this);

        this.state = {
            selectedProductsLeft: [],
            selectedProductsRight: []
        }
    }

    componentDidMount() {
        if (this.props.productList.length === 0) {
            this.props.getProductList();
        }
    }

    resetInputs(){
        this.setState({
            selectedProductsLeft: [],
            selectedProductsRight: []
        });
    }

    onProductSelect(){

    }

    rightClicked(){
        // move selected products from left to right
        let products = this.state.selectedProductsLeft;
        if(products !== null) {
            if (products.length > 0) {
                this.props.addSelectedInventoryCountProducts(products);
            }
        }

        // reset selected products
        this.setState({
            selectedProductsLeft: []
        });
    }

    doubleRightClicked(){
        // move all products from left to right
        let products = this.props.productList;
        if(products !== null) {
            if (products.length > 0) {
                this.props.addSelectedInventoryCountProducts(products);
            }
        }

        // reset selected products
        this.setState({
            selectedProductsLeft: []
        });
    }

    leftClicked(){
        // move selected products from right to left
        let products = this.state.selectedProductsRight;
        if(products !== null) {
            if (products.length > 0) {
                this.props.removeSelectedInventoryCountProducts(products);
            }
        }

        // reset selected products
        this.setState({
            selectedProductsRight: []
        });
    }

    doubleLeftClicked(){
        // move all products from right to left
        let products = this.props.selectedProducts;
        if(products !== null) {
            if (products.length > 0) {
                this.props.removeSelectedInventoryCountProducts(products);
            }
        }

        // reset selected products
        this.setState({
            selectedProductsRight: []
        });
    }

    render() {
        // find subtracted list
        let availableProducts = [];
        if(this.props.productList.length > 0){
            availableProducts = this.props.productList.filter(a => !this.props.selectedProducts.map(b=>b.sku).includes(a.sku))
        }

        return (
            <div>
                {
                    this.props.partialCount &&

                    <div className="p-grid p-fluid">
                        <div className="p-col-12">
                            move products from the list on the left to the right to include in count
                        </div>
                        <div className="p-col-5">
                            <DataTable value={availableProducts} paginator={true} rows={5}
                                       style={{height: '300px', width: '100%'}}
                                       selection={this.state.selectedProductsLeft}
                                       onSelectionChange={e => this.setState({selectedProductsLeft: e.value})}
                                       onRowSelect={this.onProductSelect}
                                       paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                       currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
                                       headerStyle={{display: 'none'}}>
                                <Column selectionMode="multiple" style={{width: '3em'}}/>
                                <Column field="sku" header="Product SKU" filter={true} filterPlaceholder="search sku"
                                        filterMatchMode="contains" style={{width: '170px'}}/>
                                <Column field="name" header="Product Name" filter={true}
                                        filterPlaceholder="search product" filterMatchMode="contains"/>
                            </DataTable>
                        </div>
                        <div className="p-col-2">
                            <div className="p-grid p-dir-col p-justify-center p-align-center">
                                <div className="p-col">
                                    <Button icon="pi pi-angle-right" onClick={this.rightClicked}/>
                                </div>
                                <div className="p-col">
                                    <Button icon="pi pi-angle-double-right" onClick={this.doubleRightClicked} />
                                </div>
                                <div className="p-col">
                                    <Button icon="pi pi-angle-double-left" onClick={this.doubleLeftClicked}/>
                                </div>
                                <div className="p-col">
                                    <Button icon="pi pi-angle-left" onClick={this.leftClicked}/>
                                </div>
                            </div>
                        </div>
                        <div className="p-col-5">
                            <DataTable value={this.props.selectedProducts} paginator={true} rows={5}
                                       style={{height: '300px', width: '100%'}}
                                       selection={this.state.selectedProductsRight}
                                       onSelectionChange={e => this.setState({selectedProductsRight: e.value})}
                                       onRowSelect={this.onProductSelect}
                                       paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                       currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
                                       headerStyle={{display: 'none'}}>
                                <Column selectionMode="multiple" style={{width: '3em'}}/>
                                <Column field="sku" header="Product SKU" filter={true} filterPlaceholder="search sku"
                                        filterMatchMode="contains" style={{width: '170px'}}/>
                                <Column field="name" header="Product Name" filter={true}
                                        filterPlaceholder="search product" filterMatchMode="contains"/>
                            </DataTable>
                        </div>
                    </div>
                }
                {
                    ! this.props.partialCount &&

                    <div className="p-grid p-fluid">
                        <div className="p-col-12">
                            full count is selected, total number of products is {this.props.productList.length}
                        </div>
                    </div>
                }
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        productsRequested: state.product.productsRequested,
        productList: state.product.productList,
        selectedProducts: state.inventoryCount.selectedInventoryCountProducts
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getProductList: () => dispatch(getProductList()),
        addSelectedInventoryCountProducts: (productsArray) => dispatch(addSelectedInventoryCountProducts(productsArray)),
        removeSelectedInventoryCountProducts: (productsArray) => dispatch(removeSelectedInventoryCountProducts(productsArray))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(ProductSelectComponent);