package ecommerce.app.backend.markets.bigcommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceVariant;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceVariantData;
import ecommerce.app.backend.inventory.TestProducts;
import ecommerce.app.backend.markets.bigcommerce.order.BCOrder;
import ecommerce.app.backend.markets.bigcommerce.order.BCOrderProduct;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceData;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public abstract class BigCommerceBaseService {

    private Logger log;

    private String baseAPIv3;
    private String baseAPIv2;
    private String accessToken;
    private String clientId;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private TestProducts testProducts;

    public BigCommerceBaseService(Class parentClass, String apipath, String clientId, String accessToken) {
        log = LoggerFactory.getLogger(parentClass);
        this.baseAPIv2 = apipath + "/v2";
        this.baseAPIv3 = apipath + "/v3";
        this.accessToken = accessToken;
        this.clientId = clientId;
    }

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", accessToken);
        headers.set("X-Auth-Client", clientId);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public BigCommerceProduct[] getProductList(int page){
        try {
            String includeFields = "id,name,sku,is_visible,price,cost_price,retail_price,sale_price,inventory_level,inventory_tracking";
            String url = baseAPIv3 + "/catalog/products?limit=50&page=" + page + "&include_fields=" + includeFields;

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<BigCommerceData> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<BigCommerceData>() { });

            BigCommerceData data = dataResponse.getBody();

            return data.getData();
        } catch (Exception e){
            log.error("Failed to get product list.", e);
        }
        return null;
    }


    public BigCommerceVariant[] getVariants(String productId){
        try {
            String includeFields = "id,product_id,sku,inventory_level,option_values";
            String url = baseAPIv3 + "/catalog/products/" + productId +"/variants?include_fields=" + includeFields;

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<BigCommerceVariantData> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<BigCommerceVariantData>() { });

            BigCommerceVariantData data = dataResponse.getBody();

            return data.getData();
        } catch (Exception e){
            log.error("Failed to get variant list.", e);
        }
        return null;
    }

    public boolean updatePrice(BigCommerceProduct bigCommerceProduct, String productSku, String costPrice, String retailPrice, String price){
        try {
            if(bigCommerceProduct == null){
                log.warn("Skip price change, no bigcommerce product found with sku " + productSku);
                return true;
            }

            if(costPrice == null && retailPrice == null && price == null){
                log.warn("Price change request ignored for bigcommerce. Price values are null.");
                return false;
            }

            String url = baseAPIv3 + "/catalog/products/{productId}";

            Map<String, String> param = new HashMap();
            param.put("productId", bigCommerceProduct.getId());

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();
            if(costPrice != null)
                jsonObject.put("cost_price", Float.parseFloat(costPrice));

            if(retailPrice != null)
                jsonObject.put("retail_price", Float.parseFloat(retailPrice));

            if(price != null)
                jsonObject.put("price", Float.parseFloat(price));

            HttpEntity requestEntity = new HttpEntity(jsonObject, getHeaders());

            ResponseEntity<ObjectNode> responseObject = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ObjectNode.class, param);

            ObjectNode response = responseObject.getBody();

            if(response.get("errors") != null){
                log.error("Failed to change product price for bigcommerce. Returning response has errors.");
                log.error("Errors " + response.get("errors").toString());
                return false;
            }

            if(costPrice!= null)
                bigCommerceProduct.setCostPrice(costPrice);

            if(retailPrice != null) {
                bigCommerceProduct.setRetailPrice(retailPrice);
            }

            if(price != null)
                bigCommerceProduct.setPrice(price);

            log.info("Price change successful for bigcommerce. [product:"+productSku+",costPrice:"+costPrice+",retailPrice:"+retailPrice+",price:"+price+"]");
            return true;
        } catch (Exception e){
            log.error("Failed to change product price for bigcommerce.", e);
            return false;
        }
    }

    public List<BCOrder> getOrders(){
        try {
            String parameters = "?sort=date_modified:desc&limit=20";
            String url = baseAPIv2 + "/orders" + parameters;

            HttpEntity requestEntity = new HttpEntity("", getHeaders());

            ResponseEntity<BCOrder[]> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<BCOrder[]>() { });

            if(dataResponse.getBody() != null){
                return Arrays.asList(dataResponse.getBody());
            }
        } catch (Exception e){
            log.error("Failed to get orders.", e);
        }
        return null;
    }

    public List<BCOrderProduct> getOrderProducts(String orderId){
        try {
            String url = baseAPIv2 + "/orders/{orderId}/products";

            Map<String, String> param = new HashMap();
            param.put("orderId", orderId);

            HttpEntity requestEntity = new HttpEntity("", getHeaders());

            ResponseEntity<BCOrderProduct[]> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<BCOrderProduct[]>() { }, param);

            if(dataResponse.getBody() != null){
                return Arrays.asList(dataResponse.getBody());
            }
        } catch (Exception e){
            log.error("Failed to get products.", e);
        }
        return null;
    }
/*
    public BigCommerceProduct getProductBySku(String sku){
        try {
            String includeFields = "id,name,sku,is_visible,price,cost_price,retail_price,sale_price,inventory_level";
            String url = baseAPIv3 + "/catalog/products?sku=" + sku + "&include_fields=" + includeFields;

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<BigCommerceData> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<BigCommerceData>() { });

            BigCommerceData data = dataResponse.getBody();

            if(data.getData() != null && data.getData().length > 0)
                return data.getData()[0];
        } catch (Exception e){
            log.error("Failed to get product list.", e);
        }
        return null;
    }
*/
    public boolean updateProductQuantity(BigCommerceProduct product, String sku, Integer amount, Boolean overwrite){
        log.info("Inventory update requested for bigcommerce product. [sku:" + sku + ",amount:" + amount + "]");
        if(testProducts.isAvailable(sku)){
            if(product != null) {
                int newQuantity = amount;
                if(overwrite == false) {
                    int currentQuantity = product.getInventoryLevel();
                    newQuantity = currentQuantity + amount;
                    if (newQuantity < 0) {
                        log.warn("There is no enough inventory in the bigcommerce store for sku " + sku + ". [currentQuantity:" + currentQuantity + ", demanded:" + amount);
                        log.warn("Set inventory to 0 for sku " + sku);
                        newQuantity = 0;
                    }
                }
                if(product.getVariantId() != null)
                    return updateVariantInventory(product, newQuantity);
                else
                    return updateInventory(product, newQuantity);
            }else {
                log.warn("No product found in bigcommerce with sku " + sku);
            }
        }
        return true;
    }

    public boolean updateInventory(BigCommerceProduct product, Integer newQuantity){
        try {
            log.info("Inventory change request for bigcommerce. [productId:"+product.getId()+",sku:"+product.getSku()+",newQuantity:"+newQuantity+"]");

            String url = baseAPIv3 + "/catalog/products/{productId}";

            Map<String, String> param = new HashMap();
            param.put("productId", product.getId());

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();
            jsonObject.put("inventory_level", newQuantity);

            HttpEntity requestEntity = new HttpEntity(jsonObject, getHeaders());

            ResponseEntity<ObjectNode> responseObject = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ObjectNode.class, param);

            ObjectNode response = responseObject.getBody();

            if(response.get("errors") != null){
                log.error("Failed to change product inventory for bigcommerce. Returning response has errors.");
                log.error("Errors " + response.get("errors").toString());
                return false;
            }
            product.setInventoryLevel(newQuantity);
            updateBaseProduct(product.getSku(), newQuantity);

            log.info("Inventory change successful for bigcommerce. [productId:"+product.getId()+",sku:"+product.getSku()+",newQuantity:"+newQuantity+"]");
            return true;
        } catch (Exception e){
            log.error("Failed to change product inventory for bigcommerce.", e);
            return false;
        }
    }

    public boolean updateVariantInventory(BigCommerceProduct product, Integer newQuantity){
        try {
            log.info("Variant inventory change request for bigcommerce. [productId:"+product.getId()+",variantId:"+product.getVariantId()+",sku:"+product.getSku()+",newQuantity:"+newQuantity+"]");

            String url = baseAPIv3 + "/catalog/products/{productId}/variants/{variantId}";

            Map<String, String> param = new HashMap();
            param.put("productId", product.getId());
            param.put("variantId", product.getVariantId());

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();
            jsonObject.put("inventory_level", newQuantity);

            HttpEntity requestEntity = new HttpEntity(jsonObject, getHeaders());

            ResponseEntity<ObjectNode> responseObject = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ObjectNode.class, param);

            ObjectNode response = responseObject.getBody();

            if(response.get("errors") != null){
                log.error("Failed to change variant inventory for bigcommerce. Returning response has errors.");
                log.error("Errors " + response.get("errors").toString());
                return false;
            }
            product.setInventoryLevel(newQuantity);
            updateBaseProduct(product.getSku(), newQuantity);

            log.info("Variant inventory change successful for bigcommerce. [productId:"+product.getId()+",variantId:"+product.getVariantId()+",sku:"+product.getSku()+",newQuantity:"+newQuantity+"]");
            return true;
        } catch (Exception e){
            log.error("Failed to change variant inventory for bigcommerce.", e);
            return false;
        }
    }

    abstract void updateBaseProduct(String sku, Integer quantity);
}
