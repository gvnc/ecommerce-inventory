import {API_URL} from '../../apiConfig';
import axios from 'axios';

export const getReport = (startDate, endDate) => {
    let requestUrl = API_URL + "/report/sales";
    let requestConfig = {
        params:{
            startDate: startDate,
            endDate: endDate
        }
    }
    return axios.get(requestUrl, requestConfig);
}

export const getReportBySku = (productSku) => {
    let requestUrl = API_URL + "/report/sales/" + productSku;
    return axios.get(requestUrl);
}