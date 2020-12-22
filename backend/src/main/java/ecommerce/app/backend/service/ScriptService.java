package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.squareup.SquareAPIService;
import ecommerce.app.backend.markets.squareup.items.SquareProduct;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.markets.vendhq.products.VendHQProduct;
import ecommerce.app.backend.model.BaseProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ScriptService {

    @Autowired
    private StoreBean storeBean;

    @Autowired
    private SquareAPIService squareAPIService;

    @Autowired
    private VendHQAPIService vendHQAPIService;

    public void syncSquareInventory(){
        Map<String, BaseProduct> productMap = storeBean.getProductsMap();
        for(String sku:productMap.keySet()){

            try {
                BaseProduct baseProduct = productMap.get(sku);
                Integer bcQuantity = baseProduct.getBigCommerceInventory();
                if (bcQuantity == null)
                    bcQuantity = 0;

                Integer sqQuantity = baseProduct.getSquareInventory();
                if (sqQuantity == null)
                    sqQuantity = 0;

                if (sqQuantity.intValue() != bcQuantity.intValue()) {
                    log.info("Difference found. SKU=" + sku + ", BigC=" + bcQuantity + ", Square=" + sqQuantity);
                    SquareProduct squareProduct = storeBean.getDetailedProductsMap().get(sku).getSquareProduct();
                    if (squareProduct != null) {
                        squareAPIService.updateInventory(squareProduct, bcQuantity);
                    } else {
                        log.info("SquareProduct not found for sku " + sku);
                    }
                } else {
                    int justChek = 1;
                }
            }catch (Exception e){
                log.error("Error", e);
            }
        }
    }

    public void syncVendInventory(){
        Map<String, BaseProduct> productMap = storeBean.getProductsMap();
        for(String sku:productMap.keySet()){

            try {
                BaseProduct baseProduct = productMap.get(sku);
                Integer bcQuantity = baseProduct.getBigCommerceInventory();
                if (bcQuantity == null)
                    bcQuantity = 0;

                Integer vendQuantity = baseProduct.getVendHQInventory();
                if (vendQuantity == null)
                    vendQuantity = 0;

                if (vendQuantity.intValue() != bcQuantity.intValue()) {
                    log.info("Difference found. SKU=" + sku + ", BigC=" + bcQuantity + ", Vend=" + vendQuantity);


                    VendHQProduct vendHQProduct = storeBean.getDetailedProductsMap().get(sku).getVendHQProduct();
                    if (vendHQProduct != null) {
                        vendHQAPIService.updateInventory(vendHQProduct, bcQuantity);
                    } else {
                        log.info("Vend product not found for sku " + sku);
                    }
                } else {
                    int justChek = 1;
                }
            }catch (Exception e){
                log.error("Error", e);
            }
        }
    }
}
