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
                    <DataTable value={this.state.resultList} paginator={true} rows={10} header={header}
                               selectionMode="single"
                               paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                               currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries">
                        <Column field="logDate" header="Sale Date" bodyStyle={columnCss}
                                filter={true} filterPlaceholder="Sale Date" />
                        <Column field="marketPlace" header="Market Place" bodyStyle={columnCss}
                                filter={true} filterPlaceholder="Market Place" />
                        <Column field="quantity" header="Quantity Sold" bodyStyle={columnCss}
                                filter={true} filterPlaceholder="Minimum" filterMatchMode="gte" />
                    </DataTable>
                }
                {
                    this.state.resultList && this.state.resultList.length === 0 &&
                    <div>This product has no Sales History yet.</div>
                }
            </div>
        )
    }
}


const header = <div className="p-clearfix" style={{lineHeight:'1.87em'}}>Sales History</div>;
const columnCss = {whiteSpace: 'nowrap', textAlign:'center'};

export default ProductSaleHistory;