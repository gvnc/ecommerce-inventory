import { combineReducers } from 'redux';

import productReducer from './productReducer';
import syncReducer from './syncReducer';
import purchaseReducer from './purchaseReducer';

export default combineReducers({
    product: productReducer,
    syncMarkets: syncReducer,
    purchase: purchaseReducer
});