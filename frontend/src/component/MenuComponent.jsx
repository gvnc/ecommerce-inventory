import React, { Component } from 'react'
import { Link, withRouter } from 'react-router-dom'
import AuthenticationService from '../service/AuthenticationService';

class MenuComponent extends Component {

    render() {
        const isUserLoggedIn = AuthenticationService.isUserLoggedIn();
        return (
            <header>
                <nav className="navbar navbar-expand-md navbar-dark bg-dark">
                    <div className="navbar-brand">market place manager</div>
                    {
                        isUserLoggedIn &&
                        <ul className="navbar-nav">
                            <li><Link className="nav-link" to="/status">Status</Link></li>
                            <li><Link className="nav-link" to="/products">Products</Link></li>
                            <li><Link className="nav-link" to="/inventoryCounts">Inventory Count</Link></li>
                            <li><Link className="nav-link" to="/purchaseOrders">Purchase Orders</Link></li>
                            <li><Link className="nav-link" to="/orderMonitoring">Order Monitoring</Link></li>
                        </ul>
                    }
                    <ul className="navbar-nav navbar-collapse justify-content-end">
                        {isUserLoggedIn && <li><Link className="nav-link" to="/logout" onClick={AuthenticationService.logout}>Logout</Link></li>}
                    </ul>
                </nav>
            </header>
        )
    }
}

export default withRouter(MenuComponent)