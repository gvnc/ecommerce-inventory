import React, { Component } from 'react'
import {getReportBySku} from "../../store/actions/reportActions";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";

class ProductSaleHistory extends Component {

    constructor() {
        super();
        this.getSalesHistory = this.getSalesHistory.bind(this);
        this.state = {
            resultList: null
        }
    }
    componentDidMount() {
        this.getSalesHistory();
    }

    getSalesHistory(){
        getReportBySku(this.props.productSku)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    let data = response.data;
                    console.log(JSON.stringify(data));
                    this.setState({resultList:data});
                }
            });
    }
    render() {
        return (
            <div style={{width:'600px'}}>
                {
                    this.state.resultList && this.state.resultList.length > 0 &&
                    <DataTable value={this.state.resultList} paginator={true} rows={10}
                               selectionMode="single"
                               paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                               currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries">
                        <Column field="logDate" header="Change Date" bodyStyle={columnCss}
                                filter={true} filterPlaceholder="Change Date" />
                        <Column field="marketPlace" header="Changed By" bodyStyle={columnCss}
                                filter={true} filterPlaceholder="Changed By" />
                        <Column field="quantity" header="Quantity " bodyStyle={columnCss}
                                filter={true} filterPlaceholder="Minimum" filterMatchMode="gte" />
                        <Column field="orderType" header="Change Type" bodyStyle={columnCss}
                                filter={true} filterPlaceholder="Change Type" />
                    </DataTable>
                }
                {
                    this.state.resultList && this.state.resultList.length === 0 &&
                    <div>This product has no Inventory Change yet.</div>
                }
            </div>
        )
    }
}


const header = <div className="p-clearfix" style={{lineHeight:'1.87em'}}>Inventory Changes</div>;
const columnCss = {whiteSpace: 'nowrap', textAlign:'center'};

export default ProductSaleHistory;