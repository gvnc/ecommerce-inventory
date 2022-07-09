package ecommerce.app.backend.markets.helcim;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.inventory.TestProducts;
import ecommerce.app.backend.markets.helcim.products.HelcimApiResponse;
import ecommerce.app.backend.markets.helcim.products.HelcimProduct;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class HelcimAPIService {

    private String apiPath;
    private String apiToken;
    private String accountId;

    private int readTimeout = 30000;
    private int connectionTimeout = 60000;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private StoreBean storeBean;

    @Autowired
    private TestProducts testProducts;

    private boolean priceUpdateEnabled;

    public HelcimAPIService(@Value("${helcim.apipath}")String apiPath,
                            @Value("${helcim.apitoken}")String apiToken,
                            @Value("${helcim.accountid}")String accountId,
                            @Value("${price.update.enabled:true}")boolean priceUpdateEnabled) {
        this.apiPath = apiPath;
        this.apiToken = apiToken;
        this.accountId = accountId;
        this.priceUpdateEnabled = priceUpdateEnabled;
    }

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("account-id", accountId);
        headers.set("api-token", apiToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        headers.setContentType(MediaType.APPLICATION_XML);
        return headers;
    }

    public List<HelcimProduct> getProductList(){
        try {
            String url = apiPath + "/product/search";

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<HelcimProduct[]> dataResponse =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<HelcimProduct[]>() { });

            if(dataResponse.getBody() != null){
                return Arrays.asList(dataResponse.getBody());
            }
        } catch (Exception e){
            log.error("Failed to get product list.", e);
            if(e instanceof ResourceAccessException)
                throw e;
        }
        return null;
    }

    public boolean updateProductQuantity(HelcimProduct product, String sku, Integer amount, Boolean overwrite){
        log.info("Inventory update requested for helcim product. [sku:{},amount:{}]", sku, amount);
        if(testProducts.isAvailable(sku)){
            if(product != null) {
                int quantityChange = amount;
                int currentQuantity = product.getStock().intValue();
                int newQuantity = currentQuantity + quantityChange;

                if(overwrite == false) {
                    if(newQuantity <0) {
                        log.warn("There is no enough inventory in the helcim store for sku {}. [currentQuantity:{}, demanded:{}]",
                                sku, currentQuantity, amount);
                    }
                } else{
                    // helcim api expects the stockChange either positive or negative
                    // to overwrite the stock with new value, find the difference between currentQuantity and new amount
                    quantityChange = amount - currentQuantity;
                    newQuantity = amount;
                }
                // TODO - variant exists or not ?

                boolean inventoryUpdated = updateInventory(product, quantityChange);
                if(inventoryUpdated){
                    product.setStock(Float.parseFloat(String.valueOf(newQuantity)));
                    BaseProduct baseProduct = storeBean.getProductsMap().get(product.getSku());
                    if (baseProduct != null) {
                        baseProduct.setHelcimInventory(newQuantity);
                    }

                    log.info("Inventory change successful for helcim. [productId:{},sku:{},quantityChange:{},newQuantity:{}]",
                            product.getId(), product.getSku(), quantityChange, newQuantity);
                } else{
                    return false;
                }
            }else {
                log.warn("No product found in helcim with sku {}", sku);
            }
        }
        return true;
    }

    private boolean operationSucceeded(HelcimApiResponse helcimApiResponse){
        return helcimApiResponse != null && "1".equals(helcimApiResponse.getResponse());
    }

    public boolean updateInventory(HelcimProduct product, Integer quantityChange){
        try {
            log.info("Inventory change request for helcim. [productId:{},sku:{},quantityChange:{}]",
                    product.getId(), product.getSku(), quantityChange);

            String url = apiPath + "/product/inventory-update";

            MultiValueMap<String, Object> dataMap= new LinkedMultiValueMap<>();
            dataMap.add("stockChange", quantityChange.toString());
            dataMap.add("productId", product.getId());
            dataMap.add("note", "UpdatedByDefconSyncApplication");

            HttpHeaders headers = getHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity requestEntity = new HttpEntity(dataMap, headers);

            ResponseEntity<HelcimApiResponse> responseObject =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, HelcimApiResponse.class);

            HelcimApiResponse apiResponse = responseObject.getBody();
            if(operationSucceeded(apiResponse)){
                return true;
            } else{
                log.error("Failed to change product inventory for helcim. Returning response message is {}", apiResponse.getResponseMessage());
            }
        } catch (Exception e){
            log.error("Failed to change product inventory for helcim.", e);
        }
        return false;
    }

    public boolean updatePrice(String productSku, String price){
        try {
            log.info("Price change requested for helcim. [product:{},price:{}]", productSku, price);
            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);
            if(detailedProduct == null)
                return false;

            HelcimProduct helcimProduct = detailedProduct.getHelcimProduct();
            if(helcimProduct == null){
                log.warn("Skip price change, no helcim product found with sku {}", productSku);
                return true;
            }

            if(price == null){
                log.warn("Price change request ignored for helcim. Price values are null.");
                return false;
            }

            if(priceUpdateEnabled) {
                String url = apiPath + "/product/modify";

                MultiValueMap<String, Object> dataMap= new LinkedMultiValueMap<>();
                dataMap.add("price", price);
                dataMap.add("salePrice", price);
                dataMap.add("productId", helcimProduct.getId());
                dataMap.add("SKU", productSku);
                dataMap.add("name", helcimProduct.getName());

                HttpHeaders headers = getHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity requestEntity = new HttpEntity(dataMap, headers);

                ResponseEntity<HelcimApiResponse> responseObject =
                        restTemplate.exchange(url, HttpMethod.POST, requestEntity, HelcimApiResponse.class);

                HelcimApiResponse apiResponse = responseObject.getBody();
                if(operationSucceeded(apiResponse)){
                    log.info("Price change successful for helcim. [product:{},price:{}]", productSku, price);
                }else{
                    log.error("Price change failed for sku {}, response is {}",
                            helcimProduct.getSku(), apiResponse.getResponseMessage());
                    return false;
                }
            }

            if(price != null) {
                helcimProduct.setPrice(Float.parseFloat(price));
                BaseProduct baseProduct = storeBean.getProductsMap().get(productSku);
                baseProduct.setHelcimPrice(Float.parseFloat(price));
            }

            return true;
        } catch (Exception e){
            log.error("Failed to change product price for helcim.", e);
            return false;
        }
    }
}
