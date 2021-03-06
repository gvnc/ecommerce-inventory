import {
    GET_INVENTORY_COUNTS,
    ADD_INVENTORY_COUNT_TO_LIST,
    GET_INVENTORY_COUNT_COMPLETED,
    UPDATE_SELECTED_INVENTORY_COUNT,
    ADD_SELECTED_INVENTORY_COUNT_PRODUCTS,
    REMOVE_SELECTED_INVENTORY_COUNT_PRODUCTS,
    SET_INVENTORY_COUNT_PRODUCT,
    SET_INVENTORY_COUNT_STATUS
} from '../actionTypes';

const initialState = {
    inventoryCounts: [],
    selectedInventoryCount: null,
    selectedInventoryCountProducts: []
};

const reducer = (state = initialState, action) => {

    switch (action.type) {
        case GET_INVENTORY_COUNTS:
            return {
                ...state,
                inventoryCounts: action.inventoryCounts
            };
        case GET_INVENTORY_COUNT_COMPLETED:
            let inventoryCounts = state.inventoryCounts;
            let inventoryCount = action.inventoryCount;
            if(inventoryCount){
                inventoryCounts = state.inventoryCounts.map((item, index) =>{
                    if(item.id === inventoryCount.id){
                        return inventoryCount;
                    }
                    return item;
                });
            }
            return {
                ...state,
                inventoryCounts: inventoryCounts,
                selectedInventoryCount: inventoryCount,
                selectedInventoryCountProducts: action.inventoryCountProducts
            };
        case ADD_INVENTORY_COUNT_TO_LIST:
            inventoryCounts = state.inventoryCounts.map((item, index) =>{
                return item;
            });
            inventoryCounts.unshift(action.inventoryCount);
            return {
                ...state,
                inventoryCounts: inventoryCounts
            }
        case UPDATE_SELECTED_INVENTORY_COUNT:
            let propertyName = action.propertyName;
            let propertyValue = action.propertyValue;
            return {
                ...state,
                selectedInventoryCount: {
                    ...state.selectedInventoryCount,
                    [propertyName]: propertyValue
                }
            };
        case ADD_SELECTED_INVENTORY_COUNT_PRODUCTS:
            let productsToAdd = action.productsToAdd;

            // create a new array to mutate the state
            let newProducts = Array.from(state.selectedInventoryCountProducts);

            // only add products if it is not already added
            productsToAdd.forEach(function (product) {
                let productExistAlready = newProducts.some(arrayItem => product.sku === arrayItem.sku);
                if(productExistAlready === false){
                    newProducts.push(product);
                }
            })
            return {
                ...state,
                selectedInventoryCountProducts: newProducts
            };
        case REMOVE_SELECTED_INVENTORY_COUNT_PRODUCTS:
            let productsToRemove = action.productsToAdd;
            let remainingProducts = state.selectedInventoryCountProducts.filter(a => !productsToRemove.map(b=>b.sku).includes(a.sku))

            return {
                ...state,
                selectedInventoryCountProducts: remainingProducts
            };
        case SET_INVENTORY_COUNT_PRODUCT:
            let inventoryCountProduct = action.inventoryCountProduct
            let inventoryCountProducts = state.selectedInventoryCountProducts.map((item, index) =>{
                if(item.id === inventoryCountProduct.id){
                    return inventoryCountProduct;
                }
                return item;
            });
            return {
                ...state,
                selectedInventoryCountProducts: inventoryCountProducts
            }
        case SET_INVENTORY_COUNT_STATUS:
            inventoryCounts = state.inventoryCounts.map((item, index) =>{
                if(item.id === action.inventoryCountId){
                    item.status = action.status;
                    return item;
                }
                return item;
            });

            return {
                ...state,
                inventoryCounts: inventoryCounts,
                selectedInventoryCount: {
                    ...state.selectedInventoryCount,
                    status: action.status
                }
            }
        default:
            return state;
    }
}

export default reducer;