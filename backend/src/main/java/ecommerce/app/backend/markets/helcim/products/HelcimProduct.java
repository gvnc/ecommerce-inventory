package ecommerce.app.backend.markets.helcim.products;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelcimProduct {
    private Integer id;
    private String name;
    private String sku;
    private Float stock;
    private Float price;
    private Float salePrice;
}
