import { createStore, compose, applyMiddleware } from "redux";
import thunk from "redux-thunk";

import rootReducer from "./reducers/rootReducer";
/*
function configureStore(state = { rotating: true }) {
    return createStore(rootReducer,state);
}

export default configureStore;

 */
export const store = createStore(rootReducer, compose(applyMiddleware(thunk)));
