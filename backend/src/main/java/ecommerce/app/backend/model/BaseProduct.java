package ecommerce.app.backend.model;

import lombok.Getter;
import lombok.Setter;

public class BaseProduct {

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private Float bigCommercePrice;

    @Getter @Setter
    private Float bigCommerceFSPrice;

    @Getter @Setter
    private Float vendHQPrice;

    @Getter @Setter
    private Float amazonCAPrice;

    @Getter @Setter
    private Float squarePrice;

    @Getter @Setter
    private Integer bigCommerceInventory;

    @Getter @Setter
    private Integer bigCommerceFSInventory;

    @Getter @Setter
    private Integer vendHQInventory;

    @Getter @Setter
    private Integer amazonCAInventory;

    @Getter @Setter
    private Integer squareInventory;

    @Getter @Setter
    private String supplierCode;

    public BaseProduct(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }
}
