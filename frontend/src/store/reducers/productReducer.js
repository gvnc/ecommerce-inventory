import { GET_PRODUCTS, SET_DETAILED_PRODUCT, COMPLETE_COMMIT_PRICE_CHANGE,
    SET_PRODUCTS_REQUESTED, COMPLETE_UPDATE_INVENTORY, UPDATE_BASEPRODUCT_PRICE} from '../actionTypes';

const initialState = {
    productsRequested: false,
    productList: [],
    detailedProduct: null,
    commitPriceResult: null,
    updateInventoryResult: null
};

const reducer = (state = initialState, action) => {

    switch (action.type) {
        case GET_PRODUCTS:
            return {
                ...state,
                productList: action.productList
            };
        case SET_DETAILED_PRODUCT:
            return {
                ...state,
                detailedProduct: action.detailedProduct
            };
        case COMPLETE_COMMIT_PRICE_CHANGE:
            return {
                ...state,
                commitPriceResult: action.commitPriceResult
            };
        case SET_PRODUCTS_REQUESTED:
            return {
                ...state,
                productsRequested: action.productsRequested
            };
        case COMPLETE_UPDATE_INVENTORY:
            return {
                ...state,
                updateInventoryResult: action.updateInventoryResult
            };
        case UPDATE_BASEPRODUCT_PRICE:
            let baseProduct = action.baseProduct;

            let productList = state.productList.map((item, index) =>{
                if(item.sku === baseProduct.sku){
                    return baseProduct;
                }
                return item;
            });
            return {
                ...state,
                productList: productList
            };
        default:
            return state;
    }
}

export default reducer;