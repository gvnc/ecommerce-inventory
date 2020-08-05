import { combineReducers } from 'redux';

import productReducer from './productReducer';
import syncReducer from './syncReducer';
import purchaseReducer from './purchaseReducer';
import inventoryCountReducer from './inventoryCountReducer';

export default combineReducers({
    product: productReducer,
    syncMarkets: syncReducer,
    purchase: purchaseReducer,
    inventoryCount: inventoryCountReducer
});