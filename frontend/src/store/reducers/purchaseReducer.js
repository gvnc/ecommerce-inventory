import { GET_PURCHASE_ORDERS, CREATE_PURCHASE_ORDER } from '../actionTypes';

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
        default:
            return state;
    }
}

export default reducer;