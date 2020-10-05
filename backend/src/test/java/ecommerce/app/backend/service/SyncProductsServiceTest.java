package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.AmazonCaService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.service.utils.ProductGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SyncProductsServiceTest {

    @Mock
    private BigCommerceAPIService bigCommerceAPIService;

    @Mock
    private BigCommerceFSAPIService bigCommerceFSAPIService;

    @Mock
    private VendHQAPIService vendHQAPIService;

    @Mock
    private AmazonCaService amazonCaService;

    @InjectMocks
    private SyncProductsService syncProductsService;

    @Before
    public void configureMockData(){
        ReflectionTestUtils.setField(syncProductsService, "syncProductsEnabled", true);
        ReflectionTestUtils.setField(syncProductsService, "storeBean", new StoreBean());
        configureBCMockData();
    }

    private void configureBCMockData(){
        BigCommerceProduct[] products = new BigCommerceProduct[2];
        BigCommerceProduct p1 = ProductGenerator.getBigCommerceProduct("1009", "My Blue Product", 3);
        BigCommerceProduct p2 = ProductGenerator.getBigCommerceProduct("1010", "My Green Product", 1);
        products[0] = p1;
        products[1] = p2;
        Mockito.when(bigCommerceAPIService.getProductList(1)).thenReturn(products);
        Mockito.when(bigCommerceAPIService.getProductList(2)).thenReturn(new BigCommerceProduct[0]);
    }

    @Test
    public void syncAllMarketPlaces(){
        syncProductsService.syncAllMarketPlaces();
    }
}
