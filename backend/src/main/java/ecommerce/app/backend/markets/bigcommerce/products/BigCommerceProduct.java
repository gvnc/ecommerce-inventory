package ecommerce.app.backend.markets.bigcommerce.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BigCommerceProduct implements Cloneable{

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private String price;

    @Getter @Setter
    private String variantId;

    @Getter @Setter
    @JsonProperty("cost_price")
    private String costPrice;

    @Getter @Setter
    @JsonProperty("retail_price")
    private String retailPrice;

    @Getter @Setter
    @JsonProperty("sale_price")
    private String salePrice;

    @Getter @Setter
    @JsonProperty("is_visible")
    private Boolean isVisible;

    @Getter @Setter
    @JsonProperty("inventory_level")
    private Integer inventoryLevel;

    @Getter @Setter
    @JsonProperty("inventory_tracking")
    private String inventoryTracking;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
