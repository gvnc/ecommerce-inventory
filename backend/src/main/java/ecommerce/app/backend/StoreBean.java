package ecommerce.app.backend;

import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.model.SyncStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StoreBean {

    @Getter @Setter
    private List<BaseProduct> productsList = new ArrayList();

    @Getter @Setter
    private Map<String, BaseProduct> productsMap = new HashMap();

    @Getter @Setter
    private Map<String, DetailedProduct> detailedProductsMap = new HashMap();

    @Getter @Setter
    private SyncStatus syncStatus = new SyncStatus();

    @Getter @Setter
    private Set<String> amazonCaQuantityUpdateSet = new HashSet<>();

    @Getter @Setter
    private Set<String> amazonCaPriceUpdateSet = new HashSet<>();

    @Getter @Setter
    private Set<String> amazonUsQuantityUpdateSet = new HashSet<>();

    @Getter @Setter
    private Set<String> amazonUsPriceUpdateSet = new HashSet<>();

    @Getter @Setter
    private Boolean orderListenerAllowed = false;
}
