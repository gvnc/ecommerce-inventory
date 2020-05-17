import { StyleSheet} from '@react-pdf/renderer';

export const styles = StyleSheet.create({

section: {
    flexDirection: 'row',
    padding: 10,
    paddingLeft: 30,
    paddingRight:30,
    margin: 10
},
expenseSection:{
    justifyContent: 'flex-end',
    flexDirection: 'row',
    margin: 10,
    paddingLeft: 30,
    paddingRight:40
},
expenseSectionView1:{
    width:'175px'
},
expenseSectionView2:{
    flexDirection: 'row',
    justifyContent:'space-between'
},
orderLine:{
    justifyContent: 'flex-end',
    flexDirection: 'row',
    margin: 4
},
orderInfoSection: {
    margin: 10,
    padding: 10
},
orderInfoHeader: {
    fontWeight: 'bold',
    fontSize: 12
},
orderInfoData: {
    fontSize: 11
},
tableCellData: {
    padding: 5
}
});