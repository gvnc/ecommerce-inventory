package ecommerce.app.backend.markets.squareup.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SquareItems {

    @Getter @Setter
    private String cursor;

    @Getter @Setter
    private SquareItemObject [] objects;

}
