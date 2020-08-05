import {
    GET_INVENTORY_COUNTS,
    GET_INVENTORY_COUNT_COMPLETED,
    UPDATE_SELECTED_INVENTORY_COUNT,
    DELETE_SELECTED_PRODUCT
} from '../actionTypes';

import {API_URL} from '../../apiConfig';
import axios from 'axios'

export const getInventoryCounts = () => {

    return (dispatch) => {

        let requestUrl = API_URL + "/inventoryCount/list";
        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(setInventoryCounts(response.data));
                }
            })
    };
}

export const setInventoryCounts = (value) => {
    return {
        type: GET_INVENTORY_COUNTS,
        inventoryCounts: value
    };
};

export const saveInventoryCount = (inventoryCount, productList, successHandler, errorHandler) => {

    return (dispatch) => {

        let requestBody = {
            inventoryCount: inventoryCount,
            productList: productList
        }

        let requestUrl = API_URL + "/inventoryCount/saveOrUpdate";
        axios.post(requestUrl, requestBody)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(setInventoryCountById(response.data));
                    successHandler("Inventory count saved.");
                } else{
                    errorHandler("Failed to save inventory count.");
                }
            })
    };
}

export const getInventoryCountById = (id) => {

    return (dispatch) => {

        let requestUrl = API_URL + "/inventoryCount/" + id;
        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(setInventoryCountById(response.data));
                }
            })
    };
}

export const setInventoryCountById = (data) => {
    return {
        type: GET_INVENTORY_COUNT_COMPLETED,
        inventoryCount: data !== null ? data.inventoryCount : null,
        inventoryCountProducts: data !== null ? data.productList : []
    };
};

export const updateSelectedInventoryCount = (propertyName, propertyValue) => {
    return {
        type: UPDATE_SELECTED_INVENTORY_COUNT,
        propertyName: propertyName,
        propertyValue: propertyValue
    };
};