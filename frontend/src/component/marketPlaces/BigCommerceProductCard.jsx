import React, { Component } from 'react'
import {InputText} from "primereact/inputtext";
import {Card} from 'primereact/card';

class BigCommerceProductCard extends Component {

    render() {

        let bigCommerce = this.props.product;
        let cardContent;

        if(bigCommerce === null){
            cardContent = <p>This product does not exist in BigCommerce.</p>;
        } else {
            let profitMargin = Math.round(((bigCommerce.retail_price - bigCommerce.cost_price) / bigCommerce.retail_price ) * 100);

            cardContent =
                <div className="p-grid p-fluid">
                    <div className="p-col-12 productProperty" style={{padding:'.75em', textAlign: 'center', backgroundColor: '#f7f7f7'}}>
                        {bigCommerce.name}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Visible</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {bigCommerce.is_visible === true ? 'Yes' : 'No'}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Inventory</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {bigCommerce.inventory_level}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Profit</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {profitMargin} %
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label htmlFor="costPrice">Cost Price</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        <InputText id="costPrice" onChange={(e) => {this.props.updateProperty('bigCommerceCostPrice', e.target.value)}}
                                   value={bigCommerce.cost_price} style={{width:'75px'}} keyfilter = {/^\d*\.?\d*$/} />
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label htmlFor="retailPrice">Retail Price</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        <InputText id="retailPrice" onChange={(e) => {this.props.updateProperty('bigCommerceRetailPrice', e.target.value)}}
                                   value={bigCommerce.retail_price} style={{width:'75px'}} keyfilter = {/^\d*\.?\d*$/} />
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label htmlFor="price">Price</label></div>
                    <div className="p-col-6" style={{padding:'.5em'}}>
                        <InputText id="price" onChange={(e) => {this.props.updateProperty('bigCommercePrice', e.target.value)}}
                                   value={bigCommerce.price} style={{width:'75px'}} keyfilter = {/^\d*\.?\d*$/} />
                    </div>
                </div>;
        }

        return (
            <Card title={this.props.title} style={{height:'100%'}}>
                {cardContent}
            </Card>
        )
    }
}

export default BigCommerceProductCard;