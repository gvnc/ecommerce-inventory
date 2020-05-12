import axios from 'axios'

import {API_URL} from '../apiConfig';
export const USER_NAME_SESSION_ATTRIBUTE_NAME = 'authenticatedUser'
export const JWT_TOKEN_SESSION_ATTRIBUTE_NAME = 'jwtToken'

class AuthenticationService {

    executeJwtAuthenticationService(username, password) {
        console.log(username);
        return axios.post(`${API_URL}/authenticate`, {
            username,
            password
        })
    }

    createBasicAuthToken(username, password) {
        return 'Basic ' + window.btoa(username + ":" + password)
    }

    registerSuccessfulLoginForJwt(username, token) {
        sessionStorage.setItem(USER_NAME_SESSION_ATTRIBUTE_NAME, username)
        sessionStorage.setItem(JWT_TOKEN_SESSION_ATTRIBUTE_NAME, token)
        this.setupAxiosInterceptors(this.createJWTToken(token))
    }

    createJWTToken(token) {
        return 'Bearer ' + token
    }

    logout() {
        sessionStorage.removeItem(USER_NAME_SESSION_ATTRIBUTE_NAME);
        sessionStorage.removeItem(JWT_TOKEN_SESSION_ATTRIBUTE_NAME);
    }

    isUserLoggedIn() {
        let user = sessionStorage.getItem(USER_NAME_SESSION_ATTRIBUTE_NAME)
        if (user === null) return false
        return true
    }

    getLoggedInUserName() {
        let user = sessionStorage.getItem(USER_NAME_SESSION_ATTRIBUTE_NAME)
        if (user === null) return ''
        return user
    }

    getJwtToken(){
        return sessionStorage.getItem(JWT_TOKEN_SESSION_ATTRIBUTE_NAME)
    }

    setupAxiosInterceptors(token) {
        axios.interceptors.request.use(
            (config) => {
                if (this.isUserLoggedIn()) {
                    config.headers.authorization = token
                }
                return config
            }
        )

    }
}

export default new AuthenticationService()