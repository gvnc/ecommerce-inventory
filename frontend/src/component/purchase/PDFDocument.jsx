import React, { Component } from 'react';
import { Page, Text, View, Document, Image } from '@react-pdf/renderer';
import { Table, TableBody, TableCell, TableHeader, DataTableCell } from "@david.kucsai/react-pdf-table";
import { styles } from './pdfDocumentStyle';

export default class PDFDocument extends Component {

    constructor() {
        super();
        this.formattedDate = this.formattedDate.bind(this);
    }

    formattedDate() {
        let today = new Date();
        let date = today.toJSON().slice(0, 10);
        let nDate = date.slice(8, 10) + '/'
            + date.slice(5, 7) + '/'
            + date.slice(0, 4);
        return nDate;
    }

    render(){

        let todate = this.formattedDate();

        let logoUrl = "http://" + window.location.hostname + ":8080/Defcon-Paintball-Logo.png";

        let address1 = "Defcon Paintball";
        let address2 = "3550 Victoria Park Avenue";
        let address3 = "Toronto, ON M2H 2N5";
        let address4 = "CANADA";

        let totalExpenses = 0;
        if(this.props.order){
            totalExpenses = totalExpenses + Number(this.props.order.salesTax);
            totalExpenses = totalExpenses + Number(this.props.order.brokerage);
            totalExpenses = totalExpenses - Number(this.props.order.discount);
            totalExpenses = totalExpenses + Number(this.props.order.duties);
            totalExpenses = totalExpenses + Number(this.props.order.shipping);
        }
        let totalProductCost = 0;
        if(this.props.orderProducts){
            this.props.orderProducts.forEach(function(p){
                totalProductCost = totalProductCost + (Number(p.orderedQuantity) * Number(p.costPrice));
            });
        }
        let orderTotal = totalProductCost + totalExpenses;
//
        return <Document>
            <Page size="A4">
                <View style={styles.section}>
                    <View style={{flexGrow: 1, justifyContent: 'center'}}>
                        <Image src={logoUrl} style={{width:'150px', height:'60px'}} />
                    </View>
                    <View style={styles.orderInfoSection}>
                        <View style={styles.orderLine}>
                            <Text style={styles.orderInfoHeader}>Purchase Order</Text>
                        </View>
                        <View style={{height:'15px'}}>
                        </View>
                        <View style={styles.orderLine}>
                            <Text style={styles.orderInfoHeader}>PO # : </Text>
                            <Text style={styles.orderInfoData}>{this.props.order.id}</Text>
                        </View>
                        <View style={styles.orderLine}>
                            <Text style={styles.orderInfoHeader}>Date : </Text>
                            <Text style={styles.orderInfoData}>{todate}</Text>
                        </View>
                        <View style={styles.orderLine}>
                            <Text style={styles.orderInfoHeader}>Requistioner : </Text>
                            <Text style={styles.orderInfoData}>{this.props.order.createdBy}</Text>
                        </View>
                    </View>
                </View>
                <View style={styles.section}>
                    <Table data={[{dummy: ""}]}>
                        <TableHeader>
                            <TableCell style={styles.tableCellData} weighting={0.50}>Company</TableCell>
                            <TableCell style={styles.tableCellData} weighting={0.50}>Ship To</TableCell>
                        </TableHeader>
                        <TableBody>
                            <TableCell style={styles.tableCellData} weighting={0.50}>
                                <Text>{address1}</Text>
                                <Text>{address2}</Text>
                                <Text>{address3}</Text>
                                <Text>{address4}</Text>
                            </TableCell>
                            <TableCell style={styles.tableCellData} weighting={0.50}>
                                <Text>{address1}</Text>
                                <Text>{address2}</Text>
                                <Text>{address3}</Text>
                                <Text>{address4}</Text>
                            </TableCell>
                        </TableBody>
                    </Table>
                </View>
                <View style={styles.section}>
                    <Table data={this.props.orderProducts} >
                        <TableHeader>
                            <TableCell style={styles.tableCellData} weighting={0.55}>Product Name</TableCell>
                            <TableCell style={styles.tableCellData} weighting={0.15}>Quantity</TableCell>
                            <TableCell style={styles.tableCellData} weighting={0.15}>Price</TableCell>
                            <TableCell style={styles.tableCellData} weighting={0.15}>Amount</TableCell>
                        </TableHeader>
                        <TableBody>
                            <DataTableCell style={styles.tableCellData} weighting={0.55} getContent={(p) => <View><Text>{p.name}</Text><Text>(SKU:{p.sku})</Text></View>}/>
                            <DataTableCell style={{...styles.tableCellData, alignItems: 'center'}} weighting={0.15} getContent={(p) => p.orderedQuantity}/>
                            <DataTableCell style={{...styles.tableCellData, alignItems: 'flex-end'}} weighting={0.15} getContent={(p) => "$" + p.costPrice.toFixed(2)}/>
                            <DataTableCell style={{...styles.tableCellData, alignItems: 'flex-end'}} weighting={0.15} getContent={(p) => "$" + (Number(p.orderedQuantity) * Number(p.costPrice)).toFixed(2)}/>
                        </TableBody>
                    </Table>
                </View>
                {
                    this.props.order.salesTax > 0 &&

                    <View style={styles.expenseSection}>
                        <View style={styles.expenseSectionView1} >
                            <View style={styles.expenseSectionView2} >
                                <Text style={styles.orderInfoHeader} >Sales Tax</Text>
                                <Text style={styles.orderInfoData} >${this.props.order.salesTax.toFixed(2)}</Text>
                            </View>
                        </View>
                    </View>
                }
                {
                    this.props.order.brokerage > 0 &&

                    <View style={styles.expenseSection}>
                        <View style={styles.expenseSectionView1} >
                            <View style={styles.expenseSectionView2} >
                                <Text style={styles.orderInfoHeader} >Brokerage</Text>
                                <Text style={styles.orderInfoData} >${this.props.order.brokerage.toFixed(2)}</Text>
                            </View>
                        </View>
                    </View>
                }
                {
                    this.props.order.discount > 0 &&

                    <View style={styles.expenseSection}>
                        <View style={styles.expenseSectionView1} >
                            <View style={styles.expenseSectionView2} >
                                <Text style={styles.orderInfoHeader} >Discount</Text>
                                <Text style={styles.orderInfoData} >${this.props.order.discount.toFixed(2)}</Text>
                            </View>
                        </View>
                    </View>
                }
                {
                    this.props.order.duties > 0 &&

                    <View style={styles.expenseSection}>
                        <View style={styles.expenseSectionView1} >
                            <View style={styles.expenseSectionView2} >
                                <Text style={styles.orderInfoHeader} >Duties</Text>
                                <Text style={styles.orderInfoData} >${this.props.order.duties.toFixed(2)}</Text>
                            </View>
                        </View>
                    </View>
                }
                {
                    this.props.order.shipping > 0 &&

                    <View style={styles.expenseSection}>
                        <View style={styles.expenseSectionView1} >
                            <View style={styles.expenseSectionView2} >
                                <Text style={styles.orderInfoHeader} >Shipping</Text>
                                <Text style={styles.orderInfoData} >${this.props.order.shipping.toFixed(2)}</Text>
                            </View>
                        </View>
                    </View>
                }
                <View style={styles.expenseSection}>
                    <View style={styles.expenseSectionView1} >
                        <View style={styles.expenseSectionView2} >
                            <Text style={{fontSize: 15}} >Total</Text>
                            <Text style={{fontSize: 15}} >${orderTotal.toFixed(2)}</Text>
                        </View>
                    </View>
                </View>
            </Page>
        </Document>
    }
}