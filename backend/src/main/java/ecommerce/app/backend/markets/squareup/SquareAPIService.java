package ecommerce.app.backend.markets.squareup;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.inventory.TestProducts;
import ecommerce.app.backend.markets.squareup.inventory.ChangeInventory;
import ecommerce.app.backend.markets.squareup.inventory.ChangeInventoryBody;
import ecommerce.app.backend.markets.squareup.inventory.ChangePhysicalCount;
import ecommerce.app.backend.markets.squareup.inventory.SquareInventoryCounts;
import ecommerce.app.backend.markets.squareup.items.*;
import ecommerce.app.backend.markets.squareup.orders.SquareOrders;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.util.LoggingInterceptor;
import ecommerce.app.backend.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class SquareAPIService {

    private String version = "2020-11-18";
    private String apiPath;
    private String authorizationToken;
    private String locationId;

    private int readTimeout = 30000;
    private int connectionTimeout = 60000;

    private RestTemplate restTemplate;

    @Autowired
    private StoreBean storeBean;

    @Autowired
    private TestProducts testProducts;

    @Value("${price.update.enabled:true}")
    private boolean priceUpdateEnabled;

    public SquareAPIService(@Value("${squareup.apipath}") String apipath, @Value("${squareup.token}") String token, @Value("${squareup.locationid}") String locationId) {
        this.apiPath = apipath;
        this.locationId = locationId;
        this.authorizationToken = "Bearer " + token;
/*
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setReadTimeout(readTimeout);
        rf.setConnectTimeout(connectionTimeout);
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(rf);
        restTemplate = new RestTemplate(factory);

        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(new LoggingInterceptor());
        restTemplate.setInterceptors(interceptors);
*/

        restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        rf.setReadTimeout(readTimeout);
        rf.setConnectTimeout(connectionTimeout);
    }

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Square-Version", version);
        headers.set("Authorization", authorizationToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public SquareItems getProductList(String cursor){
        try {
            String url = apiPath + "/catalog/list?types=ITEM";
            if(cursor != null && !cursor.equals("initial")){
                url = url + "&cursor=" + cursor;
            }

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<SquareItems> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<SquareItems>() { });

            return dataResponse.getBody();
        } catch (Exception e){
            log.error("Failed to get product list.", e);
            if(e instanceof ResourceAccessException)
                throw e;
        }
        return null;
    }

    public SquareInventoryCounts getInventoryList(String cursor){
        try {
            String url = apiPath + "/inventory/batch-retrieve-counts";


            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();

            if(cursor != null && !cursor.equals("initial")){
                jsonObject.put("cursor", cursor);
            }

            HttpEntity requestEntity = new HttpEntity(jsonObject, getHeaders());

            ResponseEntity<SquareInventoryCounts> dataResponse =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<SquareInventoryCounts>() { });

            return dataResponse.getBody();
        } catch (Exception e){
            log.error("Failed to get product list.", e);
            if(e instanceof ResourceAccessException)
                throw e;
        }
        return null;
    }

    public boolean updateProductQuantity(SquareProduct product, String sku, Integer amount, Boolean overwrite){
        log.info("Inventory update requested for squareup product. [sku:" + sku + ",amount:" + amount + "]");
        if(testProducts.isAvailable(sku)){
            if(product == null){
                log.warn("Product can not be found with sku " + sku);
                return false;
            }

            int newQuantity = amount;
            if(overwrite == false) {
                int currentQuantity = 0;
                if(product != null)
                    currentQuantity = product.getInventory();

                newQuantity = currentQuantity + amount;
                if (newQuantity < 0) {
                    log.warn("There is no enough inventory in the squareup store for sku " + sku + ". [currentQuantity:" + currentQuantity + ", demanded:" + amount + "]");
                    log.warn("Set inventory to 0 for sku " + sku);
                    newQuantity = 0;
                }
            }
            return updateInventory(product, newQuantity);
        }
        return true;
    }

    public boolean updateInventory(SquareProduct product, Integer newQuantity){
        try {
            log.info("Inventory update requested for squareup product. [sku:" + product.getSku() + ",amount:" + newQuantity + "]");
            String url = apiPath + "/inventory/batch-change";

            ChangeInventoryBody requestBody = getInventoryUpdateBody(product.getVariationId(), newQuantity);
            HttpEntity requestEntity = new HttpEntity(requestBody, getHeaders());

            ResponseEntity<SquareInventoryCounts> dataResponse =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<SquareInventoryCounts>() { });

            SquareInventoryCounts counts = dataResponse.getBody();

            if(counts != null && counts.getCounts() != null){ // update successful, so update inmemory objects
                log.info("Inventory update successful for squareup product. [sku:" + product.getSku() + ",amount:" + newQuantity + "]");
                product.setInventory(newQuantity);
                BaseProduct baseProduct = storeBean.getProductsMap().get(product.getSku());
                if(baseProduct != null){
                    baseProduct.setSquareInventory(newQuantity);
                }
                return true;
            }
        } catch (Exception e){
            log.error("Failed to change inventory.", e);
        }
        return false;
    }

    private ChangeInventoryBody getInventoryUpdateBody(String variationId, Integer newAmount){

        ChangePhysicalCount changePhysicalCount = new ChangePhysicalCount();
        changePhysicalCount.setLocationId(locationId);
        changePhysicalCount.setCatalogObjectId(variationId);
        changePhysicalCount.setQuantity(newAmount.toString());
        changePhysicalCount.setOccurredAt(Utils.getNowAsSquareupString());

        ChangeInventory changeInventory = new ChangeInventory();
        changeInventory.setPhysicalCount(changePhysicalCount);

        ChangeInventoryBody changeInventoryBody = new ChangeInventoryBody();
        changeInventoryBody.setIdempotencyKey(UUID.randomUUID().toString());
        ChangeInventory [] changes = new ChangeInventory[1];
        changes[0] = changeInventory;
        changeInventoryBody.setChanges(changes);

        return changeInventoryBody;
    }

    public SquareItemVariation getProductById(String variationId){
        try {
            String url = apiPath + "/catalog/object/" + variationId;

            HttpEntity requestEntity = new HttpEntity("", getHeaders());

            ResponseEntity<SquareItemVariationObject> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<SquareItemVariationObject>() { });

            SquareItemVariationObject squareItemVariationObject = dataResponse.getBody();
            if(squareItemVariationObject != null)
                return squareItemVariationObject.getObject();
        } catch (Exception e){
            log.error("Failed to get product by variation id.", e);
        }
        return null;
    }

    public SquareOrders getOrders(Date updateDate){
        try {
            String url = apiPath + "/orders/search";
            String updateDateStr = Utils.getDateAsSquareupString(updateDate);
            String requestBody = getOrderRequestBody(updateDateStr);
            HttpEntity requestEntity = new HttpEntity(requestBody, getHeaders());

            ResponseEntity<SquareOrders> dataResponse =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<SquareOrders>() { });

            return dataResponse.getBody();
        } catch (Exception e){
            log.error("Failed to get orders.", e);
        }
        return null;
    }

    private String getOrderRequestBody(String updateDate){
        return "{" +
                "\"location_ids\": [" +
                "      \"" + locationId + "\"" +
                "    ]," +
                "    \"query\": {" +
                "      \"filter\": {" +
                "        \"date_time_filter\": {" +
                "          \"updated_at\": {" +
                "            \"start_at\": \"" + updateDate + "\"" +
                "          }" +
                "        }," +
                "        \"state_filter\": {" +
                "          \"states\": [" +
                "            \"COMPLETED\"" +
                "          ]" +
                "        }" +
                "      }," +
                "      \"sort\": {" +
                "        \"sort_field\": \"UPDATED_AT\"," +
                "        \"sort_order\": \"ASC\"" +
                "      }" +
                "    }" +
                "  }";
    }

    public boolean updatePrice(String productSku, String newPrice){
        try {
            log.info("Price change request for squareup. [product:"+productSku+",price:"+newPrice+"]");
            String url = apiPath + "/catalog/object";

            DetailedProduct product = storeBean.getDetailedProductsMap().get(productSku);
            if(product == null || product.getSquareProduct() == null){
                throw new Exception("Squareup product can not be found in memory.");
            }

            if(priceUpdateEnabled) {
                String variationId = product.getSquareProduct().getVariationId();
                SquareItemVariation itemVariation = getProductById(variationId);
                if (itemVariation.getItemVariationData().getPriceMoney() == null)
                    throw new Exception("Price money is missing.");

                Long newPriceLong = Utils.dollarToCents(newPrice);
                itemVariation.getItemVariationData().getPriceMoney().setAmount(newPriceLong);

                ChangePriceBody changePriceBody = new ChangePriceBody();
                changePriceBody.setObject(itemVariation);
                changePriceBody.setIdempotencyKey(UUID.randomUUID().toString());

                HttpEntity requestEntity = new HttpEntity(changePriceBody, getHeaders());

                ResponseEntity<ChangePriceResponse> dataResponse =
                        restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ChangePriceResponse>() {});

                if (dataResponse.getBody() != null) {
                    log.info("Price change successful for squareup. [product:" + productSku + ",price:" + newPrice + "]");
                    product.getSquareProduct().setPrice(Float.parseFloat(newPrice));
                    BaseProduct baseProduct = storeBean.getProductsMap().get(productSku);
                    baseProduct.setSquarePrice(Float.parseFloat(newPrice));
                    return true;
                }
            }
        } catch (Exception e){
            log.error("Failed to get update price.", e);
        }
        return false;
    }

}
