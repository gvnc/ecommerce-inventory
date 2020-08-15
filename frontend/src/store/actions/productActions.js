import { GET_PRODUCTS, SET_DETAILED_PRODUCT, COMPLETE_COMMIT_PRICE_CHANGE,
    SET_PRODUCTS_REQUESTED, COMPLETE_UPDATE_INVENTORY, UPDATE_BASEPRODUCT_PRICE} from '../actionTypes';

import {API_URL} from '../../apiConfig';
import axios from 'axios'

export const getProductList = () => {

    return (dispatch) => {
        dispatch(setProductsRequested(true));
        let requestUrl = API_URL + "/products/list";
        console.log("API_URL:" + API_URL);
        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(updateProductList(response.data));
                }
            })
    };
}

export const setProductsRequested = (value) => {
    return {
        type: SET_PRODUCTS_REQUESTED,
        productsRequested: value
    };
};

export const updateProductList = (data) => {
    return {
        type: GET_PRODUCTS,
        productList: data,
        productsRequested: false
    };
};

export const getDetailedProduct = (productSku) => {
    return (dispatch) => {

        let requestUrl = API_URL + "/products/" + productSku;

        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(updateDetailedProduct(response.data));
                }
            })
    };
}

export const updateDetailedProduct = (data) => {
    return {
        type: SET_DETAILED_PRODUCT,
        detailedProduct: data
    };
};

export const commitPriceChange = (productSku, propertyChanges, noFeedback) => {

    return (dispatch) => {

        let requestUrl = API_URL + "/products/" + productSku + "/changePrice";
        axios.post(requestUrl, propertyChanges)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    if(noFeedback !== true)
                        dispatch(completeCommitPriceChange(response.data));
                }
            })
    };
}

export const completeCommitPriceChange = (data) => {
    return {
        type: COMPLETE_COMMIT_PRICE_CHANGE,
        commitPriceResult: data
    };
};

export const updateInventory = (productSku, inventory, noFeedback) => {
    return (dispatch) => {

        let requestUrl = API_URL + "/products/" + productSku + "/updateInventory";
        let requestBody = {"inventory" : inventory};
        axios.post(requestUrl, requestBody)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    if(noFeedback !== true)
                        dispatch(completeUpdateInventory(response.data));
                }
            })
    };
}

export const completeUpdateInventory = (data) => {
    return {
        type: COMPLETE_UPDATE_INVENTORY,
        updateInventoryResult: data
    };
};

export const updateBaseProductPrice = (baseProduct) => {
    return {
        type: UPDATE_BASEPRODUCT_PRICE,
        baseProduct:baseProduct
    };
};