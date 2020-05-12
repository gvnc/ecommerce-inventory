import { combineReducers } from 'redux';

import productReducer from './productReducer';
import syncReducer from './syncReducer';

export default combineReducers({
    product: productReducer,
    syncMarkets: syncReducer
});