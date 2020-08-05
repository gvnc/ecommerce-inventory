import {
    GET_INVENTORY_COUNTS,
    CREATE_PURCHASE_ORDER,
    ADD_SELECTED_PURCHASE_PRODUCTS,
    UPDATE_SELECTED_PURCHASE_ORDER,
    UPDATE_SELECTED_PO_PRODUCT,
    DELETE_SELECTED_PRODUCT,
    DELETE_PURCHASE_ORDER,
    GET_INVENTORY_COUNT_COMPLETED,
    UPDATE_SELECTED_INVENTORY_COUNT
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
        default:
            return state;
    }
}

export default reducer;