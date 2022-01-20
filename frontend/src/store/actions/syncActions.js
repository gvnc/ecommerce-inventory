import { GET_SYNC_STATUS, GET_ORDERS} from '../actionTypes';

import {getProductList} from "./productActions"
import {API_URL} from '../../apiConfig';
import axios from 'axios'

export const getSyncStatus = () => {

    return (dispatch) => {

        let requestUrl = API_URL + "/syncStatus";

        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(updateSyncStatus(response.data, false));
                }
            })
    };
}

export const updateSyncStatus = (data, syncInProgress) => {
    return {
        type: GET_SYNC_STATUS,
        syncStatus: data,
        syncInProgress: syncInProgress
    };
};

export const startSync = () => {

    return (dispatch) => {

        let requestUrl = API_URL + "/startSync";

        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(updateSyncStatus(response.data, false));
                    dispatch(getProductList());
                }
            })
    };
}

export const syncFromMaster = () => {

    return (dispatch) => {

        let requestUrl = API_URL + "/startSyncFromMaster";

        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(updateSyncStatus(response.data, false));
                    dispatch(getProductList());
                }
            })
    };
}

export const getOrders = () => {

    return (dispatch) => {

        let requestUrl = API_URL + "/getOrders";

        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(updateOrders(response.data));
                }
            })
    };
}

export const updateOrders = (data) => {
    return {
        type: GET_ORDERS,
        orders: data
    };
};