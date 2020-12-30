import React, { Component } from 'react'
import {connect} from "react-redux";
import {commitPriceChange, updateDetailedProduct, completeCommitPriceChange, updateInventory, completeUpdateInventory} from "../../store/actions/productActions"
import {Dialog} from "primereact/dialog";
import {Card} from 'primereact/card';
import BigCommerceProductCard from "./BigCommerceProductCard";
import VendHQProductCard from "./VendHQProductCard";
import AmazonProductCard from "./AmazonProductCard";
import InventoryUpdate from "./InventoryUpdate";
import {TabView,TabPanel} from 'primereact/tabview';
import SquareProductCard from "./SquareProductCard";
import ProductSaleHistory from "./ProductSaleHistory";

class ProductDetailDialog extends Component {

    constructor() {
        super();
        this.state = {
            "updateInventoryInProgress" : false,
            "savePriceInProgress" : false,
            activeIndex: 0
        }
        this.save = this.save.bind(this);
        this.updateProperty = this.updateProperty.bind(this);
        this.updateInventoryEvent = this.updateInventoryEvent.bind(this);
    }

    componentDidMount() {
        //console.log("component did mount");
    }

    save() {
        let detailedProduct = this.props.detailedProduct;
        let priceParameters = {};

        if(this.state.activeIndex === 0) {
            let marketPlace = "BigCommerce";

            let bigCommerceProduct = detailedProduct.bigCommerceProduct;
            let bigCommerceFsProduct = detailedProduct.bigCommerceFSProduct;
            let vendHQProduct = detailedProduct.vendHQProduct;
            let squareProduct = detailedProduct.squareProduct;

            // use product price parameters in redux store to request the backend
            // locale store does not work for some reason
            // if bigcommerce is not null use it, else use vendhq, they should be sync
            if(bigCommerceProduct !== undefined && bigCommerceProduct !== null){
                priceParameters = {
                    "bigCommercePrice" : bigCommerceProduct.price,
                    "bigCommerceRetailPrice" : bigCommerceProduct.retail_price,
                    "bigCommerceCostPrice" : bigCommerceProduct.cost_price,
                    "marketPlace" : marketPlace
                };
            } else if(bigCommerceFsProduct !== undefined && bigCommerceFsProduct !== null){
                priceParameters = {
                    "bigCommercePrice" : bigCommerceFsProduct.price,
                    "bigCommerceRetailPrice" : bigCommerceFsProduct.retail_price,
                    "bigCommerceCostPrice" : bigCommerceFsProduct.cost_price,
                    "marketPlace" : marketPlace
                };
            } else if(vendHQProduct !== undefined && vendHQProduct !== null){
                priceParameters = {
                    "bigCommerceRetailPrice" : vendHQProduct.price_including_tax,
                    "bigCommerceCostPrice" : vendHQProduct.supply_price,
                    "marketPlace" : marketPlace
                };
            } else if(squareProduct !== undefined && squareProduct !== null){
                priceParameters = {
                    "bigCommerceRetailPrice" : squareProduct.price,
                    "marketPlace" : marketPlace
                };
            }

            this.props.commitPriceChange(this.props.detailedProduct.sku, priceParameters);
            this.setState({"savePriceInProgress": true});

        } else if(this.state.activeIndex === 1) {
            let marketPlace = "Amazon";
            let amazonCaProduct = detailedProduct.amazonCaProduct;
            if(amazonCaProduct !== undefined && amazonCaProduct !== null){
                priceParameters = {
                    "amazonPrice" : amazonCaProduct.price,
                    "marketPlace" : marketPlace
                };

                this.props.commitPriceChange(this.props.detailedProduct.sku, priceParameters);
                this.setState({"savePriceInProgress": true});
            }
        }
        //this.props.onHideEvent();
    }

    updateInventoryEvent() {
        let inventoryLevel = this.props.detailedProduct.inventoryLevel;
        console.log("inventoryLevel " + inventoryLevel);
        if(inventoryLevel !== ""){
            this.props.updateInventory(this.props.detailedProduct.sku, inventoryLevel);
            this.setState({"updateInventoryInProgress": true});
        }
    }

    updateProperty(property, value) {
        let detailedProduct = this.props.detailedProduct;
        // copy price values between vendhq and bigcommerce
        if(property === "bigCommercePrice"){
            if(detailedProduct.bigCommerceProduct !== null)
                detailedProduct.bigCommerceProduct.price = value;

            if(detailedProduct.bigCommerceFSProduct !== null)
                detailedProduct.bigCommerceFSProduct.price = value;

            // vendhq does not have price, but retail_price
        }
        if(property === "bigCommerceRetailPrice"){
            if(detailedProduct.bigCommerceProduct !== null)
                detailedProduct.bigCommerceProduct.retail_price = value;

            if(detailedProduct.bigCommerceFSProduct !== null)
                detailedProduct.bigCommerceFSProduct.retail_price = value;

            if(detailedProduct.vendHQProduct !== null)
                detailedProduct.vendHQProduct.price_including_tax = value;

            if(detailedProduct.squareProduct !== null)
                detailedProduct.squareProduct.price = value;
        }
        if(property === "bigCommerceCostPrice"){
            if(detailedProduct.bigCommerceProduct !== null)
                detailedProduct.bigCommerceProduct.cost_price = value;

            if(detailedProduct.bigCommerceFSProduct !== null)
                detailedProduct.bigCommerceFSProduct.cost_price = value;

            if(detailedProduct.vendHQProduct !== null)
                detailedProduct.vendHQProduct.supply_price = value;
        }
        // copy price values between amazon
        if(property === "amazonPrice"){
            if(detailedProduct.amazonCaProduct !== null)
                detailedProduct.amazonCaProduct.price = value;

            if(detailedProduct.amazonUsProduct !== null)
                detailedProduct.amazonUsProduct.price = value;
        }
        if(property === "inventoryLevel"){
            detailedProduct.inventoryLevel = value;
        }

        this.props.updateDetailedProduct(detailedProduct);

        // required to change the state and render
        this.setState({[property]: value});
    }

    handleInventoryUpdateResult(){
        let result = this.props.updateInventoryResult;
        let messages = [];
        messages.push({life:6000, severity: result.bigCommerceInventoryUpdate, summary: "BigCommerce", detail: this.getDetailMessageForInventory(result.bigCommerceInventoryUpdate)});
        messages.push({life:6000, severity: result.bigCommerceFSInventoryUpdate, summary: "BigCommerce FS", detail: this.getDetailMessageForInventory(result.bigCommerceFSInventoryUpdate)});

        // remove comment out to enable square
        // messages.push({life:6000, severity: result.squareInventoryUpdate, summary: "SquareUp", detail: this.getDetailMessageForInventory(result.squareInventoryUpdate)});
        messages.push({life:6000, severity: result.vendhqInventoryUpdate, summary: "VendHQ", detail: this.getDetailMessageForInventory(result.vendhqInventoryUpdate)});
        messages.push({life:6000, severity: result.amazonUsInventoryUpdate, summary: "Amazon Us", detail: this.getDetailMessageForInventory(result.amazonUsInventoryUpdate)});
        messages.push({life:6000, severity: result.amazonCaInventoryUpdate, summary: "Amazon Ca", detail: this.getDetailMessageForInventory(result.amazonCaInventoryUpdate)});

        this.props.setGrowlMessage(messages);

        // set inventory in store manually to update the cards
        let detailedProduct = this.props.detailedProduct;
        if(detailedProduct.bigCommerceProduct !== null)
            detailedProduct.bigCommerceProduct.inventory_level = detailedProduct.inventoryLevel;
        if(detailedProduct.bigCommerceFSProduct !== null)
            detailedProduct.bigCommerceFSProduct.inventory_level = detailedProduct.inventoryLevel;
        if(detailedProduct.vendHQProduct !== null){
            if(detailedProduct.vendHQProduct.product_inventory !== null){
                detailedProduct.vendHQProduct.product_inventory.inventory_level = detailedProduct.inventoryLevel;
            }
        }
        if(detailedProduct.squareProduct !== null){
            detailedProduct.squareProduct.inventory = detailedProduct.inventoryLevel;
        }

        this.props.updateDetailedProduct(detailedProduct);

        /*
        if(result.finalResult === "success")
            this.props.onHideEvent();
        */
        this.props.completeUpdateInventory(null);
        this.setState({"updateInventoryInProgress": false});
    }

    handleCommitPriceResult(){
        let result = this.props.commitPriceResult;
        let messages = [];
        messages.push({life:6000, severity: result.bigCommercePriceChange, summary: "BigCommerce", detail: this.getDetailMessage(result.bigCommercePriceChange)});
        messages.push({life:6000, severity: result.bigCommerceFSPriceChange, summary: "BigCommerce FS", detail: this.getDetailMessage(result.bigCommerceFSPriceChange)});
        // remove comment out to enable square
        // messages.push({life:6000, severity: result.squarePriceChange, summary: "SquareUp", detail: this.getDetailMessage(result.squarePriceChange)});
        messages.push({life:6000, severity: result.vendhqPriceChange, summary: "VendHQ", detail: this.getDetailMessage(result.vendhqPriceChange)});
        messages.push({life:6000, severity: result.amazonUsPriceChange, summary: "Amazon Us", detail: this.getDetailMessage(result.amazonUsPriceChange)});
        messages.push({life:6000, severity: result.amazonCaPriceChange, summary: "Amazon Ca", detail: this.getDetailMessage(result.amazonCaPriceChange)});

        this.props.setGrowlMessage(messages);

        /*
        if(result.finalResult === "success")
            this.props.onHideEvent();
         */
        this.props.completeCommitPriceChange(null);
        this.setState({"savePriceInProgress": false});
    }

    getDetailMessage(operationResult){
        if(operationResult === "success")
            return "Price change committed.";

        if(operationResult === "error")
            return "Failed to change price.";

        return "Price change not available";
    }

    getDetailMessageForInventory(operationResult){
        if(operationResult === "success")
            return "Inventory change successful.";

        if(operationResult === "error")
            return "Failed to update inventory.";

        return "Inventory update not available";
    }

    render() {
        if(this.props.detailedProduct === null){
            return <div/>;
        }
        let dialogHeader = "Product Details - SKU " + this.props.detailedProduct.sku;

        if(this.props.commitPriceResult !== null){
            this.handleCommitPriceResult();
            //return <div/>;
        }

        if(this.props.updateInventoryResult !== null){
            this.handleInventoryUpdateResult();
            //return <div/>;
        }

        let dialogFooter =  <InventoryUpdate save={this.save} updateInventoryEvent={this.updateInventoryEvent}
                                             updateProperty={this.updateProperty} inventoryLevel={this.props.detailedProduct.inventoryLevel}
                                             updateInventoryInProgress={this.state.updateInventoryInProgress}
                                             savePriceInProgress={this.state.savePriceInProgress}/>;

        return (
            <Dialog visible={this.props.visibleProperty} maximized={true} header={dialogHeader} modal={true}
                    footer={dialogFooter} onHide={this.props.onHideEvent}>
                <TabView activeIndex={this.state.activeIndex} style={{width:'1200px'}}
                         onTabChange={(e) => this.setState({activeIndex: e.index})}>
                    <TabPanel header="BC-Vend">
                        <div className="p-grid">
                            <div className="p-col-4">
                                <BigCommerceProductCard title="BigCommerce" product={this.props.detailedProduct.bigCommerceProduct} updateProperty={this.updateProperty} />
                            </div>
                            <div className="p-col-4">
                                <VendHQProductCard product={this.props.detailedProduct.vendHQProduct} updateProperty={this.updateProperty} />
                            </div>
                            <div className="p-col-4">
                                <BigCommerceProductCard title="BigCommerce FS" product={this.props.detailedProduct.bigCommerceFSProduct} updateProperty={this.updateProperty} />
                            </div>
                            { // remove comment out to enable square
                                /*
                            <div className="p-col-4">
                                <SquareProductCard product={this.props.detailedProduct.squareProduct} updateProperty={this.updateProperty} />
                            </div>

                                 */
                            }
                        </div>
                    </TabPanel>
                    <TabPanel header="Amazon">
                        <div className="p-grid">
                            <div className="p-col-2"></div>
                            <div className="p-col-4">
                                <AmazonProductCard title="Amazon CA" product={this.props.detailedProduct.amazonCaProduct} updateProperty={this.updateProperty} />
                            </div>
                            <div className="p-col-4">
                                <Card title="Amazon US" style={{height:'100%'}} >
                                    <p>to be implemented</p>
                                </Card>
                            </div>
                            <div className="p-col-2"></div>
                        </div>
                    </TabPanel>
                    <TabPanel header="Inventory Changes">
                        <ProductSaleHistory productSku={this.props.detailedProduct.sku} />
                    </TabPanel>
                </TabView>
            </Dialog>
        )
    }
}

const mapStateToProps = state => {
    return {
        detailedProduct: state.product.detailedProduct,
        commitPriceResult: state.product.commitPriceResult,
        updateInventoryResult: state.product.updateInventoryResult
    };
};

const mapDispatchToProps = dispatch => {
    return {
        commitPriceChange: (productSku, propertyChanges) => dispatch(commitPriceChange(productSku, propertyChanges)),
        updateDetailedProduct: (detailedProduct) => dispatch(updateDetailedProduct(detailedProduct)),
        completeCommitPriceChange: (commitPriceResult) => dispatch(completeCommitPriceChange(commitPriceResult)),
        updateInventory: (productSku, inventoryChange) => dispatch(updateInventory(productSku, inventoryChange)),
        completeUpdateInventory: (inventoryUpdateResult) => dispatch(completeUpdateInventory(inventoryUpdateResult))
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(ProductDetailDialog);