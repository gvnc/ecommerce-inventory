package ecommerce.app.backend.service.utils;

import ecommerce.app.backend.markets.amazon.products.AmazonProduct;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.markets.vendhq.products.VendHQInventory;
import ecommerce.app.backend.markets.vendhq.products.VendHQProduct;

public class ProductGenerator {

    public static BigCommerceProduct getBigCommerceProduct(String sku, String name, Integer inventory){
        BigCommerceProduct bigCommerceProduct = new BigCommerceProduct();
        bigCommerceProduct.setSku(sku);
        bigCommerceProduct.setName(name);
        bigCommerceProduct.setInventoryLevel(inventory);
        bigCommerceProduct.setIsVisible(true);
        bigCommerceProduct.setRetailPrice("10.5");
        bigCommerceProduct.setPrice("10.5");
        bigCommerceProduct.setSalePrice("10.5");

        return bigCommerceProduct;
    }

    public static VendHQProduct getVendHQProduct(String sku, String name, Integer inventory){
        VendHQProduct vendHQProduct = new VendHQProduct();
        vendHQProduct.setSku(sku);
        vendHQProduct.setName(name);

        VendHQInventory vendHQInventory = new VendHQInventory();
        vendHQInventory.setInventoryLevel(inventory);
        vendHQProduct.setInventory(vendHQInventory);

        return vendHQProduct;
    }

    public static AmazonProduct getAmazonProduct(String sku, String name, Integer inventory){
        AmazonProduct amazonProduct = new AmazonProduct();
        amazonProduct.setSku(sku);
        amazonProduct.setName(name);
        amazonProduct.setQuantity(inventory);

        return amazonProduct;
    }
}
