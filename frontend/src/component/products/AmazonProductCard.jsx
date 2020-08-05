import React, { Component } from 'react'
import {InputText} from "primereact/inputtext";
import {Card} from 'primereact/card';

class AmazonProductCard extends Component {

    render() {

        let amazonProduct = this.props.product;
        let cardContent;

        if(amazonProduct === null){
            cardContent = <p>This product does not exist in Amazon.</p>;
        } else {
            cardContent =
                <div className="p-grid p-fluid">
                    <div className="p-col-12 productProperty" style={{padding:'.75em', textAlign: 'center', backgroundColor: '#f7f7f7'}}>
                        {amazonProduct.name}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>ASIN</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {amazonProduct.id}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Listing Id</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {amazonProduct.listingId}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Is FBA</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {amazonProduct.isFulfilledByAmazon === true ? 'Yes' : 'No'}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Inventory</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        {amazonProduct.quantity}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label htmlFor="price">Price</label></div>
                    <div className="p-col-6" style={{padding:'.5em'}}>
                        <InputText id="price" onChange={(e) => {this.props.updateProperty('amazonPrice', e.target.value)}}
                                   value={amazonProduct.price} style={{width:'75px'}} keyfilter = {/^\d*\.?\d*$/} />
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

export default AmazonProductCard;