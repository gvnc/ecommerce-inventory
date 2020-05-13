import { GET_PURCHASE_ORDERS, CREATE_PURCHASE_ORDER} from '../actionTypes';

import {API_URL} from '../../apiConfig';
import axios from 'axios'
import {completeCommitPriceChange} from "./productActions";

export const getPurchaseOrders = () => {

    return (dispatch) => {

        let requestUrl = API_URL + "/purchase/orders";
        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(setPurchaseOrders(response.data));
                }
            })
    };
}

export const setPurchaseOrders = (value) => {
    return {
        type: GET_PURCHASE_ORDERS,
        purchaseOrders: value
    };
};

export const createPurchaseOrder = (purchaseOrder,successHandler, errorHandler) => {

    return (dispatch) => {

        let requestUrl = API_URL + "/purchase/orders/create";
        axios.post(requestUrl, purchaseOrder)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    // add returning order to orders list
                    dispatch(createPurchaseOrderCompleted(response.data));
                    successHandler();
                } else{
                    errorHandler();
                }

            })
    };
}

export const createPurchaseOrderCompleted = (value) => {
    return {
        type: CREATE_PURCHASE_ORDER,
        order: value
    };
};
