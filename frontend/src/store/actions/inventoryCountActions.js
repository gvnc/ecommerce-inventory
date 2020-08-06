import {
    GET_INVENTORY_COUNTS,
    GET_INVENTORY_COUNT_COMPLETED,
    UPDATE_SELECTED_INVENTORY_COUNT,
    ADD_INVENTORY_COUNT_TO_LIST,
    ADD_SELECTED_INVENTORY_COUNT_PRODUCTS,
    REMOVE_SELECTED_INVENTORY_COUNT_PRODUCTS
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
                    // if this is create not update, update the list
                    if(inventoryCount.id === null && response.data !== null){
                        dispatch(addNewInventoryCountToList(response.data.inventoryCount));
                    }
                    successHandler("Inventory count saved.");
                } else{
                    errorHandler("Failed to save inventory count.");
                }
            })
    };
}

export const getInventoryCountById = (id) => {

    return (dispatch) => {

        let requestUrl = API_URL + "/inventoryCount/get/" + id;

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
    // add to list if necessary !!
    return {
        type: GET_INVENTORY_COUNT_COMPLETED,
        inventoryCount: data !== null ? data.inventoryCount : null,
        inventoryCountProducts: data !== null ? data.productList : []
    };
};

export const addNewInventoryCountToList = (inventoryCount) => {
    return {
        type: ADD_INVENTORY_COUNT_TO_LIST,
        inventoryCount: inventoryCount
    };
};

export const updateSelectedInventoryCount = (propertyName, propertyValue) => {
    return {
        type: UPDATE_SELECTED_INVENTORY_COUNT,
        propertyName: propertyName,
        propertyValue: propertyValue
    };
};

export const addSelectedInventoryCountProducts = (productsArray) => {
    return {
        type: ADD_SELECTED_INVENTORY_COUNT_PRODUCTS,
        productsToAdd: productsArray
    };
};

export const removeSelectedInventoryCountProducts = (productsArray) => {
    return {
        type: REMOVE_SELECTED_INVENTORY_COUNT_PRODUCTS,
        productsToAdd: productsArray
    };
};