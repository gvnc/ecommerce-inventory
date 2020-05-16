import {
    GET_PURCHASE_ORDERS,
    CREATE_PURCHASE_ORDER,
    ADD_SELECTED_PURCHASE_PRODUCTS,
    UPDATE_SELECTED_PURCHASE_ORDER,
    UPDATE_SELECTED_PO_PRODUCT,
    GET_PURCHASE_ORDER_COMPLETED,
    DELETE_SELECTED_PRODUCT,
    DELETE_PURCHASE_ORDER
} from '../actionTypes';

import {API_URL} from '../../apiConfig';
import axios from 'axios'

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

export const addSelectedPurchaseProducts = (productsArray) => {
    return {
        type: ADD_SELECTED_PURCHASE_PRODUCTS,
        productsToAdd: productsArray
    };
};

export const updateSelectedPurchaseOrder = (id, propertyName, propertyValue) => {
    return {
        type: UPDATE_SELECTED_PURCHASE_ORDER,
        id: id,
        propertyName: propertyName,
        propertyValue: propertyValue
    };
};

export const updateSelectedPOProduct = (sku, propertyName, propertyValue) => {
    return {
        type: UPDATE_SELECTED_PO_PRODUCT,
        sku: sku,
        propertyName: propertyName,
        propertyValue: propertyValue
    };
};

export const savePurchaseOrder = (purchaseOrder, productList, successHandler, errorHandler) => {

    return (dispatch) => {
        let orderId = purchaseOrder.id;
        let requestBody = {
            purchaseOrder: purchaseOrder,
            productList: productList
        }

        let requestUrl = API_URL + "/purchase/orders/" + orderId + "/save";
        axios.post(requestUrl, requestBody)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(setPurchaseOrder(response.data));
                    successHandler("Purchase order saved.");
                } else{
                    errorHandler("Failed to save purchase order.");
                }
            })
    };
}

export const getPurchaseOrderById = (orderId) => {

    return (dispatch) => {

        let requestUrl = API_URL + "/purchase/orders/" + orderId;
        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    dispatch(setPurchaseOrder(response.data));
                }
            })
    };
}

export const setPurchaseOrder = (data) => {

    return {
        type: GET_PURCHASE_ORDER_COMPLETED,
        order: data !== null ? data.purchaseOrder : null,
        orderProducts: data !== null ? data.productList : []
    };
};


export const deleteSelectedProduct = (sku) => {

    return {
        type: DELETE_SELECTED_PRODUCT,
        sku: sku
    };
};

export const deletePurchaseOrderProduct = (orderId, product, successHandler, errorHandler) => {

    return (dispatch) => {
        let requestUrl = API_URL + "/purchase/orders/" + orderId + "/products/" + product.id;
        axios.delete(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response && response.data === "success"){
                    dispatch(deleteSelectedProduct(product.sku));
                    successHandler("Product deleted.");
                } else {
                    errorHandler("Failed to delete product.");
                }
            })
    };
}

export const submitPurchaseOrder = (purchaseOrder, productList, successHandler, errorHandler) => {

    return (dispatch) => {
        let orderId = purchaseOrder.id;
        let requestBody = {
            purchaseOrder: purchaseOrder,
            productList: productList
        }

        let requestUrl = API_URL + "/purchase/orders/" + orderId + "/submit";
        axios.post(requestUrl, requestBody)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response && response.data) {
                    dispatch(setPurchaseOrder(response.data));
                    successHandler("Purchase order submitted.");
                } else{
                    errorHandler("Failed to submit purchase order.");
                }
            })
    };
}

export const receivePurchaseProducts = (orderId, receiveList, successHandler, errorHandler) => {

    return (dispatch) => {
        let requestBody = {
            receiveList: receiveList
        }

        let requestUrl = API_URL + "/purchase/orders/" + orderId + "/receive";
        axios.post(requestUrl, requestBody)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response && response.data) {
                    dispatch(setPurchaseOrder(response.data));
                    successHandler("Received products.");
                } else{
                    errorHandler("Failed to receive products.");
                }
            })
    };
}

export const cancelPurchaseOrder = (orderId, successHandler, errorHandler) => {

    return (dispatch) => {
        let requestUrl = API_URL + "/purchase/orders/" + orderId + "/cancel";
        axios.post(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response && response.data) {
                    dispatch(setPurchaseOrder(response.data));
                    successHandler("Purchase order cancelled.");
                } else{
                    errorHandler("Failed to cancel order.");
                }
            })
    };
}

export const deletePurchaseOrder = (orderId, successHandler, errorHandler) => {

    return (dispatch) => {
        let requestUrl = API_URL + "/purchase/orders/" + orderId;
        axios.delete(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response && response.data === "success"){
                    dispatch(deletePurchaseOrderFromStore(orderId));
                    successHandler("Purchase Order deleted.");
                } else {
                    errorHandler("Failed to delete purchase order.");
                }
            })
    };
}

export const deletePurchaseOrderFromStore = (orderId) => {

    return {
        type: DELETE_PURCHASE_ORDER,
        orderId: orderId
    };
};
