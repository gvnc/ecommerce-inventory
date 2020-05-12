import React, { Component } from 'react';
import {Card} from 'primereact/card';
import {InputText} from "primereact/inputtext";

class VendHQProductCard extends Component {

    render() {

        let vendhq = this.props.product;
        let cardContent;

        if(vendhq === null){
            cardContent = <p>This product does not exist in Vend HQ.</p>;
        } else {
            let inventoryLevel = "0";
            if(vendhq.product_inventory !== undefined && vendhq.product_inventory !== null){
                inventoryLevel = vendhq.product_inventory.inventory_level;
            }

            let profitMargin = Math.round(((vendhq.price_including_tax - vendhq.supply_price) / vendhq.price_including_tax ) * 100);

            cardContent =
                <div className="p-grid p-fluid">
                    <div className="p-col-12 productProperty" style={{padding:'.75em', textAlign: 'center', backgroundColor: '#f7f7f7'}}>
                        {vendhq.name}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Active</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {vendhq.active === true ? 'Yes' : 'No'}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Inventory</label></div>
                    <div className="p-col-3" style={{padding:'.75em'}}>{inventoryLevel}</div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Profit</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {profitMargin} %
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Cost Price</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        <InputText id="costPrice" onChange={(e) => {this.props.updateProperty('bigCommerceCostPrice', e.target.value)}}
                                   value={vendhq.supply_price} style={{width:'75px'}} keyfilter = {/^\d*\.?\d*$/} />
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label htmlFor="price">Price</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        <InputText id="retailPrice" onChange={(e) => {this.props.updateProperty('bigCommerceRetailPrice', e.target.value)}}
                                   value={vendhq.price_including_tax} style={{width:'75px'}} keyfilter = {/^\d*\.?\d*$/} />
                    </div>
                </div>;
        }

        return (
            <Card title="Vend HQ" style={{height:'100%'}}>
                {cardContent}
            </Card>
        )
    }
}

export default VendHQProductCard;