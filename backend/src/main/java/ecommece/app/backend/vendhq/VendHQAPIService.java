package ecommece.app.backend.vendhq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecommece.app.backend.StoreBean;;
import ecommece.app.backend.TestProducts;
import ecommece.app.backend.model.DetailedProduct;
import ecommece.app.backend.vendhq.products.*;
import ecommece.app.backend.vendhq.sales.VendHQSale;
import ecommece.app.backend.vendhq.sales.VendHQSales;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class VendHQAPIService {

    private String baseAPIv09;
    private String baseAPIv20;
    private String authorizationToken;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private StoreBean storeBean;

    public VendHQAPIService(@Value("${vend.apipath}") String apipath, @Value("${vend.token}") String token) {
        this.authorizationToken = "Bearer " + token;
        this.baseAPIv09 = apipath;
        this.baseAPIv20 = apipath + "/2.0";
    }

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public VendHQProductsData getProductList(long version){
        try {
            String url = baseAPIv20 + "/products?page_size=100&after=" + version;

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<VendHQProductsData> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<VendHQProductsData>() { });

            return dataResponse.getBody();
        } catch (Exception e){
            log.error("Failed to get product list.", e);
        }
        return null;
    }

    public boolean updatePrice(String productSku, String supplyPrice, String price){
        try {
            log.info("Price change request for vendhq. [product:"+productSku+",costPrice:"+supplyPrice+",price:"+price+"]");
            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(productSku);
            if(detailedProduct == null)
                return false;

            VendHQProduct vendHQProduct = detailedProduct.getVendHQProduct();
            if(vendHQProduct == null){
                log.warn("Skip price change, no vendhq product found with sku " + productSku);
                return true;
            }

            if(supplyPrice == null && price == null){
                log.warn("Price change request ignored for vendhq. Price values are null.");
                return false;
            }

            String url = baseAPIv09 + "/products";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();
            jsonObject.put("id", vendHQProduct.getId());

            if(supplyPrice != null)
                jsonObject.put("supply_price", supplyPrice);

            if(price != null)
                jsonObject.put("retail_price", Float.parseFloat(price));

            HttpEntity requestEntity = new HttpEntity(jsonObject, getHeaders());

            ResponseEntity<VendHQResponseProduct> responseObject = restTemplate.exchange(url, HttpMethod.POST, requestEntity, VendHQResponseProduct.class);

            VendHQResponseProduct responseProduct = responseObject.getBody();

            if(responseProduct == null){
                log.error("Failed to change product price for vendhq. Returning response is null.");
                return false;
            }

            vendHQProduct.setPrice(Float.parseFloat(price));
            vendHQProduct.setSupplyPrice(Float.parseFloat(supplyPrice));
            log.info("Price change successful for vendhq. [product:"+productSku+",costPrice:"+supplyPrice+",price:"+price+"]");
            return true;
        } catch (Exception e){
            log.error("Failed to change product price for vendhq.", e);
            return false;
        }
    }

    public List<VendHQSale> getSales(Long afterVersion){
        try {
            String url = baseAPIv20 + "/sales?page_size=20&after=" + afterVersion;

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<VendHQSales> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<VendHQSales>() { });

            if(dataResponse.getBody() != null) {
                VendHQSales sales = dataResponse.getBody();
                if (sales != null) {
                    return Arrays.asList(sales.getData());
                }
            }
        } catch (Exception e){
            log.error("Failed to get sales list.", e);
        }
        return null;
    }

    public VendHQProduct getProductById(String productId){
        try {
            String url = baseAPIv09 + "/products/{productId}";

            Map<String, String> param = new HashMap();
            param.put("productId", productId);

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<VendHQProducts> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<VendHQProducts>() { }, param);

            VendHQProducts data = dataResponse.getBody();

            if(data.getProducts() != null && data.getProducts().length > 0)
                return data.getProducts()[0];
        } catch (Exception e){
            log.error("Failed to get product by id " + productId , e);
        }
        return null;
    }

    public VendHQProduct getProductBySku(String sku){
        try {
            String url = baseAPIv09 + "/products?sku=" + sku;

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<VendHQProducts> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<VendHQProducts>() { });

            VendHQProducts data = dataResponse.getBody();

            if(data.getProducts() != null && data.getProducts().length > 0)
                return data.getProducts()[0];
        } catch (Exception e){
            log.error("Failed to get product by sku " + sku , e);
        }
        return null;
    }

    public VendHQInventory getProductInventoryById(String productId){
        try {
            String url = baseAPIv20 + "/products/{productId}/inventory";

            Map<String, String> param = new HashMap();
            param.put("productId", productId);

            HttpEntity<String> requestEntity = new HttpEntity<>("", getHeaders());

            ResponseEntity<VendHQInventoryData> dataResponse =
                    restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<VendHQInventoryData>() { }, param);

            VendHQInventoryData data = dataResponse.getBody();

            if(data.getData() != null && data.getData().length > 0)
                return data.getData()[0];
        } catch (Exception e){
            log.error("Failed to get product by id " + productId , e);
        }
        return null;
    }

    public boolean updateProductQuantity(String sku, Integer amount, Boolean overwrite){
        log.info("Inventory update requested for vendhq product. [sku:" + sku + ",amount:" + amount + "]");
        if(TestProducts.isAvailable(sku)){

            VendHQProduct product = getProductBySku(sku);
            if(product == null){
                log.warn("Product can not be found with sku " + sku);
                return false;
            }

            VendHQInventory vendHQInventory = getProductInventoryById(product.getId());
            product.setInventory(vendHQInventory);

            if(product != null) {
                int newQuantity = amount;
                if(overwrite == false) {
                    int currentQuantity = vendHQInventory.getInventoryLevel();
                    newQuantity = currentQuantity + amount;
                    if (newQuantity < 0) {
                        log.warn("There is no enough inventory in the vendhq store for sku " + sku + ". [currentQuantity:" + currentQuantity + ", demanded:" + amount + "]");
                        log.warn("Set inventory to 0 for sku " + sku);
                        newQuantity = 0;
                    }
                }
                return updateInventory(product, newQuantity);
            }else {
                log.warn("No product found in vendhq with sku " + sku);
            }
        }
        return true;
    }

    public boolean updateInventory(VendHQProduct product, Integer newQuantity){

        try {
            log.info("Inventory change request for vendhq. [productId:"+product.getId()+",sku:"+product.getSku()+",newQuantity:"+newQuantity+"]");

            String url = baseAPIv09 + "/products";

            VendHQInventory inventory;
            if(product.getInventory() != null)
                inventory = product.getInventory();
            else {
                log.warn("Can not find inventory product. [productId:"+product.getId()+",sku:"+product.getSku()+"]");
                return true;
            }

            inventory.setInventoryLevel(newQuantity);
            inventory.setCount(newQuantity);
            VendHQInventory[] inventoryArray = new VendHQInventory[1];
            inventoryArray[0] = inventory;

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();
            jsonObject.put("id", product.getId());
            jsonObject.putPOJO("inventory", inventoryArray);

            HttpEntity requestEntity = new HttpEntity(jsonObject, getHeaders());

            ResponseEntity<VendHQResponseProduct> responseObject =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, VendHQResponseProduct.class);

            VendHQResponseProduct responseProduct = responseObject.getBody();

            if(responseProduct == null){
                log.error("Failed to change product inventory for vendhq. Returning response is null.");
                return false;
            }

            // set values in the store bean as well !!
            /*
            DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(product.getSku());
            if(detailedProduct == null) {
                detailedProduct = new DetailedProduct(product.getSku(), product.getName(), null,product);
                storeBean.getDetailedProductsMap().put(product.getSku(), detailedProduct);
                BaseProduct baseProduct = new BaseProduct(product.getSku(), product.getName());
                storeBean.getProductsMap().put(product.getSku(), baseProduct);
                storeBean.getProductsList().add(baseProduct);
            } else {
                detailedProduct.getVendHQProduct().setInventory(inventory);
            }

             */

            log.info("Inventory change successful for vendhq. [productId:"+product.getId()+",sku:"+product.getSku()+",newQuantity:"+newQuantity+"]");
            return true;
        } catch (Exception e){
            log.error("Failed to change product inventory for vendhq.", e);
            return false;
        }
    }
}
