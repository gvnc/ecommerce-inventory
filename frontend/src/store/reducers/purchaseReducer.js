import { GET_PURCHASE_ORDERS, CREATE_PURCHASE_ORDER, ADD_SELECTED_PURCHASE_PRODUCTS } from '../actionTypes';

const initialState = {
    orders: [],
    selectedOrder: null,
    selectedOrderProducts: []
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
            let selectedOrderProducts = state.selectedOrderProducts;
            let newProducts = Array.from(selectedOrderProducts);

            productsToAdd.forEach(function (product) {
                let productExistAlready = newProducts.some(arrayItem => product.sku == arrayItem.sku);
                if(productExistAlready === false){
                    newProducts.push(product);
                }
            })
            return {
                ...state,
                selectedOrderProducts: newProducts
            };
        default:
            return state;
    }
}

export default reducer;