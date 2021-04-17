import React, { Component } from 'react'
import AuthenticationService from '../service/AuthenticationService';
import {Redirect} from "react-router-dom";
import {Card} from "primereact/card";
import {Button} from "primereact/button";

class LoginComponent extends Component {

    constructor(props) {
        super(props)

        this.state = {
            username: 'admin',
            password: '',
            hasLoginFailed: false,
            showSuccessMessage: false
        }

        this.handleChange = this.handleChange.bind(this)
        this.loginClicked = this.loginClicked.bind(this)
    }

    handleChange(event) {
        this.setState(
            {
                [event.target.name]
                    : event.target.value
            }
        )
    }

    loginClicked() {
         AuthenticationService
             .executeJwtAuthenticationService(this.state.username, this.state.password)
             .then((response) => {
                 AuthenticationService.registerSuccessfulLoginForJwt(this.state.username, response.data.token)
                 this.props.history.push(`/status`)
             }).catch(() => {
                 this.setState({ showSuccessMessage: false })
                 this.setState({ hasLoginFailed: true })
             })

    }

    render() {
        if (AuthenticationService.isUserLoggedIn()) {
            return <Redirect to="/status" />
        }
        let footerContent = <div style={{textAlign: 'right', marginRight:'50px'}}>
                                <Button label="Login" onClick={this.loginClicked} />
                            </div>;

        return (
            <div className="p-grid p-justify-center">
                <Card title="Login" style={{width:'400px', marginTop:'50px'}} footer={footerContent}>
                    <div className="p-grid p-fluid">
                        <div className="p-col-4">
                            <label>User Name</label>
                        </div>
                        <div className="p-col-8">
                            <input type="text" name="username" value={this.state.username} onChange={this.handleChange} />
                        </div>
                        <div className="p-col-4">
                            <label>Password</label>
                        </div>
                        <div className="p-col-8">
                            <input type="password" name="password" value={this.state.password} onChange={this.handleChange} />
                        </div>
                        {/*<ShowInvalidCredentials hasLoginFailed={this.state.hasLoginFailed}/>*/}
                        {this.state.hasLoginFailed && <div className="p-col-12 alert alert-warning">Invalid Credentials</div>}
                        {this.state.showSuccessMessage && <div className="p-col-12">Login Sucessful</div>}
                        {/*<ShowLoginSuccessMessage showSuccessMessage={this.state.showSuccessMessage}/>*/}
                    </div>
                </Card>
            </div>
        )
    }
}

export default LoginComponent