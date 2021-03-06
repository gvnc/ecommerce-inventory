import {
    GET_PURCHASE_ORDERS,
    CREATE_PURCHASE_ORDER,
    ADD_SELECTED_PURCHASE_PRODUCTS,
    UPDATE_SELECTED_PURCHASE_ORDER,
    UPDATE_SELECTED_PO_PRODUCT,
    GET_PURCHASE_ORDER_COMPLETED,
    DELETE_SELECTED_PRODUCT,
    DELETE_PURCHASE_ORDER,
    FILE_ATTACHMENT_UPLOADED
} from '../actionTypes';

const initialState = {
    orders: [],
    selectedOrder: null,
    selectedOrderProducts: [],
    selectedOrderFileAttachment: null
};

const reducer = (state = initialState, action) => {

    switch (action.type) {
        case GET_PURCHASE_ORDERS:
            return {
                ...state,
                orders: action.purchaseOrders
            };
        case CREATE_PURCHASE_ORDER:
            let order = action.order;
            let orders = state.orders;
            orders.splice(0,0,order);
            return {
                ...state,
                orders: orders,
                selectedOrder: order
            };
        case ADD_SELECTED_PURCHASE_PRODUCTS:
            let productsToAdd = action.productsToAdd;

            // create a new array to mutate the state
            let newProducts = Array.from(state.selectedOrderProducts);

            // only add products if it not already added
            productsToAdd.forEach(function (product) {
                let productExistAlready = newProducts.some(arrayItem => product.sku === arrayItem.sku);
                if(productExistAlready === false){
                    newProducts.push(product);
                }
            })
            return {
                ...state,
                selectedOrderProducts: newProducts
            };
        case UPDATE_SELECTED_PURCHASE_ORDER:
            let id = action.id;
            let propertyName = action.propertyName;
            let propertyValue = action.propertyValue;

            order = null;
            orders = state.orders.map((item, index) =>{
               if(item.id === id){
                   order = {
                       ...item,
                       [propertyName]: propertyValue
                   }
                   return order;
               }
               return item;
            });
            return {
                ...state,
                orders: orders,
                selectedOrder: order
            };
        case UPDATE_SELECTED_PO_PRODUCT:
            let sku = action.sku;
            propertyName = action.propertyName;
            propertyValue = action.propertyValue;

            let product = null;
            let products = state.selectedOrderProducts.map((item, index) =>{
                if(item.sku === sku){
                    product = {
                        ...item,
                        [propertyName]: propertyValue
                    }
                    return product;
                }
                return item;
            });

            return {
                ...state,
                selectedOrderProducts: products
            };
        case GET_PURCHASE_ORDER_COMPLETED:
            orders = state.orders;
            order = action.order;
            if(order){
                let trackingNumbers = order.trackingNumbers;
                if(trackingNumbers && trackingNumbers.length>0){
                    order.trackingNumberArray = trackingNumbers.split(",");
                }
                orders = state.orders.map((item, index) =>{
                    if(item.id === order.id){
                        return order;
                    }
                    return item;
                });
            }
            return {
                ...state,
                orders: orders,
                selectedOrder: order,
                selectedOrderProducts: action.orderProducts,
                selectedOrderFileAttachment: action.orderFileAttachment
            };
        case DELETE_SELECTED_PRODUCT:
            sku = action.sku;
            products = state.selectedOrderProducts.filter( p => p.sku !== sku);
            return {
                ...state,
                selectedOrderProducts: products
            };
        case DELETE_PURCHASE_ORDER:
            let orderId = action.orderId;
            orders = state.orders.filter(item => item.id !== orderId);

            return {
                ...state,
                orders: orders
            };
        case FILE_ATTACHMENT_UPLOADED:
            return {
                ...state,
                selectedOrderFileAttachment: {
                    filename: action.filename
                }
            };
        default:
            return state;
    }
}

export default reducer;