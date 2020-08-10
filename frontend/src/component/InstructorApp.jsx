import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import LoginComponent from './LoginComponent';
import LogoutComponent from './LogoutComponent';
import MenuComponent from './MenuComponent';
import MarketPlaceSyncStatus from './MarketPlaceSyncStatus';
import AuthenticatedRoute from './AuthenticatedRoute';
import ListProducts from "./products/ListProducts";
import AuthenticationService from "../service/AuthenticationService";
import OrderMonitoring from "./OrderMonitoring";
import PurchaseOrders from "./purchase/PurchaseOrders";
import InventoryCounts from "./inventoryCount/InventoryCounts";
import InventoryCountInProgress from "./inventoryCount/InventoryCountInProgress";
import InventoryCountReview from "./inventoryCount/InventoryCountReview";

class InstructorApp extends Component {

    constructor(props) {
        super(props);
        const jwtToken = AuthenticationService.getJwtToken();
        if(jwtToken){
            const jwtTokenCreated = AuthenticationService.createJWTToken(jwtToken);
            AuthenticationService.setupAxiosInterceptors(jwtTokenCreated);
        }
    }

    render() {
        return (
            <>
                <Router>
                    <>
                        <MenuComponent />
                        <Switch>
                            <Route path="/" exact component={LoginComponent} />
                            <Route path="/login" exact component={LoginComponent} />
                            <AuthenticatedRoute path="/logout" exact component={LogoutComponent} />
                            <AuthenticatedRoute path="/products" exact component={ListProducts} />
                            <AuthenticatedRoute path="/inventoryCounts" exact component={InventoryCounts} />
                            <AuthenticatedRoute path="/purchaseOrders" exact component={PurchaseOrders} />
                            <AuthenticatedRoute path="/orderMonitoring" exact component={OrderMonitoring} />
                            <AuthenticatedRoute path="/inventoryCountInProgress/:inventoryCountId" exact component={InventoryCountInProgress} />
                            <AuthenticatedRoute path="/inventoryCountReview/:inventoryCountId" exact component={InventoryCountReview} />
                            <AuthenticatedRoute path="/*" exact component={MarketPlaceSyncStatus} />
                        </Switch>
                    </>
                </Router>
            </>
        )
    }
}

export default InstructorApp