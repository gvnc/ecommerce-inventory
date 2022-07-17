import React, { Component } from 'react';
import {Card} from 'primereact/card';
import {InputText} from "primereact/inputtext";

class HelcimProductCard extends Component {

    render() {

        let helcim = this.props.product;
        let cardContent;

        if(helcim === null){
            cardContent = <p>This product does not exist in Helcim.</p>;
        } else {
            cardContent =
                <div className="p-grid p-fluid">
                    <div className="p-col-12 productProperty" style={{padding:'.75em', textAlign: 'center', backgroundColor: '#f7f7f7'}}>
                        {helcim.name}
                    </div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label>Inventory</label></div>
                    <div className="p-col-3" style={{padding:'.75em'}}>{helcim.stock}</div>

                    <div className="p-col-6 productProperty" style={{padding:'.75em'}}><label htmlFor="price">Price</label></div>
                    <div className="p-col-6" style={{padding:'.75em'}}>
                        <InputText id="retailPrice" onChange={(e) => {this.props.updateProperty('bigCommerceRetailPrice', e.target.value)}}
                                   value={helcim.price} style={{width:'75px'}} keyfilter = {/^\d*\.?\d*$/} />
                    </div>
                </div>;
        }

        return (
            <Card title="Helcim" style={{height:'100%'}}>
                {cardContent}
            </Card>
        )
    }
}

export default HelcimProductCard;