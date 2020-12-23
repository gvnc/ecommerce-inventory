import { StyleSheet} from '@react-pdf/renderer';

export const styles = StyleSheet.create({

    section: {
        flexDirection: 'row',
        padding: 10,
        paddingLeft: 30,
        paddingRight:30,
        margin: 10
    },
    infoLine:{
        justifyContent: 'flex-start',
        flexDirection: 'row',
        margin: 4
    },
    infoSection: {
        margin: 10,
        padding: 10
    },
    infoHeader: {
        fontWeight: 'bold',
        fontSize: 12
    },
    infoData: {
        fontSize: 11
    },
    tableCellData: {
        padding: 5
    }
});