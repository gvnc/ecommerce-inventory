import React, { Component } from 'react'
import { connect } from "react-redux";
import { getOrders} from "../store/actions/syncActions"
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";

class MarketPlaceSyncStatus extends Component {

    componentDidMount() {
        this.props.getOrders();
    }

    render() {
        let header = <div className="p-clearfix" style={{lineHeight:'1.87em'}}>Order Monitoring in Market Places</div>;
        let columnCss = {whiteSpace: 'nowrap', textAlign:'center'};
        return (
            <div>
                <div className="content-section implementation">
                    {this.props.orders &&
                        <DataTable value={this.props.orders} paginator={true} rows={10} header={header}
                                   selectionMode="single"
                                   paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                                   currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries">
                            <Column bodyStyle={columnCss} field="dateModified" header="Date"/>
                            <Column bodyStyle={columnCss} field="marketPlace" header="Market Place"/>
                            <Column bodyStyle={columnCss} field="orderId" header="Order Id"/>
                            <Column bodyStyle={columnCss} field="totalPrice" header="Total Price"/>
                            <Column bodyStyle={columnCss} field="status" header="Status"/>
                        </DataTable>
                    }
                </div>
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        orders: state.syncMarkets.orders
    };
};

const mapDispatchToProps = dispatch => {
    return {
        getOrders: () => dispatch(getOrders())
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(MarketPlaceSyncStatus);
