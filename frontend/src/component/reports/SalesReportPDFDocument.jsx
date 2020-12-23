import React, { Component } from 'react';
import { Page, Text, View, Document } from '@react-pdf/renderer';
import { Table, TableBody, TableCell, TableHeader, DataTableCell } from "@david.kucsai/react-pdf-table";
import { styles } from './salesPdfStyle';

export default class SalesReportPDFDocument extends Component {

    constructor() {
        super();
    }

    render(){
        
        return <Document>
            <Page size="A4">
                <View style={styles.section}>
                    <View style={styles.infoSection}>
                        <View style={styles.infoLine}>
                            <Text style={styles.infoHeader}>Sales Report</Text>
                        </View>
                        <View style={{height:'15px'}}>
                        </View>
                        <View style={styles.infoLine}>
                            <Text style={styles.infoHeader}>Start Date : </Text>
                            <Text style={styles.infoData}>{this.props.startDate.toString()}</Text>
                        </View>
                        <View style={styles.infoLine}>
                            <Text style={styles.infoHeader}>End Date : </Text>
                            <Text style={styles.infoData}>{this.props.endDate.toString()}</Text>
                        </View>
                    </View>
                </View>
                <View style={styles.section}>
                    <Table data={this.props.reportList} >
                        <TableHeader>
                            <TableCell style={styles.tableCellData} weighting={0.25}>Product Sku</TableCell>
                            <TableCell style={styles.tableCellData} weighting={0.60}>ProductName</TableCell>
                            <TableCell style={styles.tableCellData} weighting={0.15}>Quantity</TableCell>
                        </TableHeader>
                        <TableBody>
                            <DataTableCell style={{...styles.tableCellData, alignItems: 'flex-start'}} weighting={0.25} getContent={(p) => p.sku}/>
                            <DataTableCell style={{...styles.tableCellData, alignItems: 'flex-start'}} weighting={0.60} getContent={(p) => p.productName}/>
                            <DataTableCell style={{...styles.tableCellData, alignItems: 'center'}} weighting={0.15} getContent={(p) => p.quantity}/>
                        </TableBody>
                    </Table>
                </View>
            </Page>
        </Document>
    }
}