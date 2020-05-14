import axios from 'axios'

import {API_URL} from '../apiConfig';

class ProductService {

    // these 2 methods are exceptional, they shouldnt be part of redux actions

    getProductBySku(productSku, eventHandler){

        let requestUrl = API_URL + "/products/" + productSku;
        axios.get(requestUrl)
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    eventHandler(response.data);
                }
            })
    }

    getProductsBySkuList(skuList, eventHandler){
        console.log(("skulist " + skuList))
        let requestUrl = API_URL + "/products/skuList";
        axios.get(requestUrl, {
            params: {
                skuList: skuList
            }
        })
            .catch(err => {
                console.log("error:" + err);
            })
            .then(response => {
                if(response) {
                    eventHandler(response.data);
                }
            })
    }
}

export default new ProductService()