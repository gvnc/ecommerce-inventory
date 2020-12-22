import {API_URL} from '../../apiConfig';
import axios from 'axios';

export const getReport = (startDate, endDate) => {
    console.log("startDate " + JSON.stringify(startDate));
    console.log("startDate " + startDate + " endDate " + endDate)
    let requestUrl = API_URL + "/report/sales";
    let requestConfig = {
        params:{
            startDate: startDate,
            endDate: endDate
        }
    }
    return axios.get(requestUrl, requestConfig);
}