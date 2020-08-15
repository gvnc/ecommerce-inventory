import React, { Component } from 'react'
import { connect } from "react-redux";
import {getDetailedProduct, getProductList, updateDetailedProduct} from "../../store/actions/productActions"
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import ProductDetailDialog from "./ProductDetailDialog";
import {Growl} from 'primereact/growl';

class ListProducts extends Component {

    constructor() {
        super();
        this.state = {};
        this.onProductSelect = this.onProductSelect.bind(this);
        this.setGrowlMessage = this.setGrowlMessage.bind(this);
    }

    componentDidMount() {
        if (this.props.productList.length === 0) {
            this.props.getProductList();
        }
    }

    onProductSelect(e){
        let selectedProduct = Object.assign({}, e.data);
        this.props.updateDetailedProduct(null);
        this.props.getDetailedProduct(selectedProduct.sku);

        // product:
        this.setState({
            displayDialog:true
        });
    }

    setGrowlMessage(messages){
        this.growl.show(messages);
    }

    render() {
        return (
            <div>
                <Growl ref={(el) => this.growl = el} />
                <div className="content-section implementation">
                    <DataTable value={this.props.productList} paginator={true} rows={10}
                               selectionMode="single" selection={this.state.selectedProduct} onSelectionChange={e => this.setState({selectedProduct: e.value})}
                               onRowSelect={this.onProductSelect}
                               paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                               currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries">
                        <Column field="sku" header="Product SKU" sortable={true} filter={true} filterPlaceholder="search sku" filterMatchMode="contains" />
                        <Column field="name" header="Product Name" sortable={true} filter={true} filterPlaceholder="search product" filterMatchMode="contains" />
                    </DataTable>

                    <ProductDetailDialog visibleProperty={this.state.displayDialog}
                                         onHideEvent={() => this.setState({displayDialog: false})} productSku={null}
                                         setGrowlMessage={(messages) => this.setGrowlMessage(messages)}
                    />
                </div>
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        productList: state.product.productList
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getProductList: () => dispatch(getProductList()),
        getDetailedProduct: (productSku) => dispatch(getDetailedProduct(productSku)),
        updateDetailedProduct: (detailedProduct) => dispatch(updateDetailedProduct(detailedProduct))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(ListProducts);