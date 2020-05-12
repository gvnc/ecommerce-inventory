package ecommece.app.backend;

import ecommece.app.backend.model.BaseOrder;
import ecommece.app.backend.model.BaseProduct;
import ecommece.app.backend.model.DetailedProduct;
import ecommece.app.backend.model.SyncStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreBean {

    @Getter @Setter
    private List<BaseProduct> productsList = new ArrayList();

    @Getter @Setter
    private Map<String, BaseProduct> productsMap = new HashMap();

    @Getter @Setter
    private Map<String, DetailedProduct> detailedProductsMap = new HashMap();

    @Getter @Setter
    private List<BaseOrder> orderStatusChanges = new ArrayList();

    @Getter @Setter
    private SyncStatus syncStatus = new SyncStatus();
}
