import React, { Component } from 'react';
import {Card} from 'primereact/card';
import {InputText} from "primereact/inputtext";

class SquareProductCard extends Component {

    render() {

        let squareup = this.props.product;
        let cardContent;

        if(squareup === null){
            cardContent = <p>This product does not exist in SquareUp.</p>;
        } else {
            cardContent =
                <div className="p-grid p-fluid">
                    <div className="p-col-12 productProperty" style={{padding:'.75em', textAlign: 'center', backgroundColor: '#f7f7f7'}}>
                        {squareup.name}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Inventory</label></div>
                    <div className="p-col-3" style={{padding:'.75em'}}>{squareup.inventory}</div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label htmlFor="price">Price</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        <InputText id="retailPrice" onChange={(e) => {this.props.updateProperty('bigCommerceRetailPrice', e.target.value)}}
                                   value={squareup.price} style={{width:'75px'}} keyfilter = {/^\d*\.?\d*$/} />
                    </div>
                </div>;
        }

        return (
            <Card title="SquareUp" style={{height:'100%'}}>
                {cardContent}
            </Card>
        )
    }
}

export default SquareProductCard;