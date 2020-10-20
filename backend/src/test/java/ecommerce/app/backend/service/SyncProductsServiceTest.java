package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.AmazonCaService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.markets.vendhq.products.*;
import ecommerce.app.backend.service.constants.SyncConstants;
import ecommerce.app.backend.service.utils.ProductGenerator;
import org.junit.Assert;
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

    private StoreBean storeBean = new StoreBean();

    @Before
    public void configureMockData(){
        ReflectionTestUtils.setField(syncProductsService, "syncProductsEnabled", true);
        ReflectionTestUtils.setField(syncProductsService, "storeBean", storeBean);
        configureBCMockData();
        configureVendHQMockData();
        configureAmazonCAMockData();
    }

    private void configureBCMockData(){
        BigCommerceProduct p1 = ProductGenerator.getBigCommerceProduct("1009", "My Blue Product", 3);
        BigCommerceProduct p2 = ProductGenerator.getBigCommerceProduct("1010", "My Green Product", 1);
        BigCommerceProduct[] products = new BigCommerceProduct[2];
        products[0] = p1;
        products[1] = p2;

        Mockito.when(bigCommerceAPIService.getProductList(1)).thenReturn(products);
        Mockito.when(bigCommerceAPIService.getProductList(2)).thenReturn(new BigCommerceProduct[0]);

        Mockito.when(bigCommerceFSAPIService.getProductList(1)).thenReturn(products);
        Mockito.when(bigCommerceFSAPIService.getProductList(2)).thenReturn(new BigCommerceProduct[0]);
    }

    private void configureVendHQMockData(){
        VendHQInventory vi1 = ProductGenerator.getVendHQInventory("1",3);
        VendHQInventory vi2 = ProductGenerator.getVendHQInventory("2",1);
        VendHQInventory[] viArray = new VendHQInventory[2];
        viArray[0] = vi1;
        viArray[1] = vi2;

        VendHQInventoryData vendHQInventoryData = new VendHQInventoryData();
        vendHQInventoryData.setData(viArray);
        vendHQInventoryData.setVersion(new VendHQVersion());

        VendHQProduct v1 = ProductGenerator.getVendHQProduct("1", "1009", "My Blue Product", 3);
        VendHQProduct v2 = ProductGenerator.getVendHQProduct("2", "1010", "My Green Product", 1);
        VendHQProduct[] vendHQProductArray = new VendHQProduct[2];
        vendHQProductArray[0] = v1;
        vendHQProductArray[1] = v2;

        VendHQProductsData vendHQProductsData = new VendHQProductsData();
        vendHQProductsData.setData(vendHQProductArray);
        vendHQProductsData.setVersion(new VendHQVersion());

        Mockito.when(vendHQAPIService.getInventoryList(Mockito.anyLong())).thenReturn(vendHQInventoryData);
        Mockito.when(vendHQAPIService.getProductList(Mockito.anyLong())).thenReturn(vendHQProductsData);
    }

    private void configureAmazonCAMockData(){
        Mockito.when(amazonCaService.getProductList()).thenReturn(null);
    }

    @Test
    public void syncAllMarketPlaces(){
        syncProductsService.syncAllMarketPlaces();

        Assert.assertEquals(storeBean.getSyncStatus().getBigCommerceSyncStatus(), SyncConstants.SYNC_COMPLETED);
        Assert.assertEquals(storeBean.getSyncStatus().getVendHQSyncStatus(), SyncConstants.SYNC_COMPLETED);
        Assert.assertEquals(storeBean.getSyncStatus().getBigCommerceFSSyncStatus(), SyncConstants.SYNC_COMPLETED);
        Assert.assertEquals(storeBean.getDetailedProductsMap().size(), 2);
    }
}
