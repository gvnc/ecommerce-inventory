import { GET_SYNC_STATUS, GET_ORDERS } from '../actionTypes';

const initialState = {
    syncStatus : null,
    syncInProgress : true,
    orders:[]
};

const reducer = (state = initialState, action) => {

    switch (action.type) {
        case GET_SYNC_STATUS:
            return {
                ...state,
                syncStatus: action.syncStatus,
                syncInProgress: action.syncInProgress
            };
        case GET_ORDERS:
            return {
                ...state,
                orders: action.orders
            };
        default:
            return state;
    }
}

export default reducer;