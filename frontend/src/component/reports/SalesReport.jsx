import React, { Component } from 'react'
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Calendar} from 'primereact/calendar';
import {Button} from "primereact/button";

import { getReport} from "../../store/actions/reportActions";
import {PDFDownloadLink} from "@react-pdf/renderer";
import SalesReportPDFDocument from "./SalesReportPDFDocument";

class SalesReport extends Component {

    constructor() {
        super();
        this.submitReport = this.submitReport.bind(this);

        this.state = {
            startDate: null,
            endDate: null,
            resultList: null
        }
    }

    submitReport(){
        getReport(this.state.startDate, this.state.endDate)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    let data = response.data;
                    this.setState({resultList:data});
                }
            });
    }

    render() {
        if(this.state.resultList)
            console.log(this.state.resultList.length);
        console.dir(this.state.startDate);

        return (
            <div className="content-section implementation" style={{margin:'30px'}}>

                <div className="p-fluid p-grid">
                    <div className="p-field p-col-3">
                        <span className="p-float-label">
                            <Calendar id="startDateCalendar" value={this.state.startDate}
                                      onChange={(e) => this.setState({startDate: e.value})} />
                            <label htmlFor="startDateCalendar">Start Date</label>
                        </span>
                    </div>
                    <div className="p-field p-col-3">
                        <span className="p-float-label">
                            <Calendar id="endDateCalendar" value={this.state.endDate}
                                      onChange={(e) => this.setState({endDate: e.value})} />
                            <label htmlFor="endDateCalendar">End Date</label>
                        </span>
                    </div>
                    <div className="p-field p-col-2">
                        <Button label="Get Report" icon="pi pi-search"  onClick={this.submitReport}/>
                    </div>
                    {
                        this.state.resultList && this.state.resultList.length > 0 &&

                        <div className="p-field p-col-2">
                            <PDFDownloadLink
                                    document={<SalesReportPDFDocument startDate={this.state.startDate} endDate={this.state.endDate} reportList={this.state.resultList} />}
                                    fileName="SalesReport.pdf">
                                {({blob, url, loading, error}) => (loading ? 'Loading document...' : 'Download Report')}
                            </PDFDownloadLink>
                        </div>
                    }
                </div>
                {
                    this.state.resultList &&
                    <DataTable value={this.state.resultList} paginator={true} rows={10} header={header}
                               selectionMode="single"
                               paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                               currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries">
                        <Column bodyStyle={componentCss.skuCol} headerStyle={componentCss.skuCol} field="sku" header="Sku"
                                filter={true} filterPlaceholder="Sku" />
                        <Column bodyStyle={componentCss.nameCol} headerStyle={componentCss.nameCol}  field="productName" header="Name"
                                filter={true} filterPlaceholder="Product Name" />
                        <Column bodyStyle={componentCss.quantityCol} headerStyle={componentCss.quantityCol}  field="quantity" header="Quantity Sold"
                                filter={true} filterPlaceholder="Minimum" filterMatchMode="gte" sortable/>
                    </DataTable>
                }
            </div>
        )
    }
}

const header = <div className="p-clearfix" style={{lineHeight:'1.87em'}}>Sales Report</div>;

const componentCss={
    skuCol:{
        height:'3.5em',
        width:'200px'
    },
    nameCol:{
        height:'3.5em',
        whiteSpace: 'nowrap'
    },
    quantityCol:{
        height:'3.5em',
        width:'200px',
        textAlign:'center'
    }
}

export default SalesReport;