package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.model.InventoryCountRequest;
import ecommerce.app.backend.repository.InventoryCountProductRepository;
import ecommerce.app.backend.repository.InventoryCountRepository;
import ecommerce.app.backend.repository.model.InventoryCount;
import ecommerce.app.backend.repository.model.InventoryCountProduct;
import ecommerce.app.backend.service.constants.InventoryCountConstants;
import ecommerce.app.backend.service.utils.ProductGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class InventoryCountServiceTest {

    @Mock
    private InventoryCountRepository inventoryCountRepository;

    @Mock
    private InventoryCountProductRepository inventoryCountProductRepository;

    @Mock
    private StoreBean storeBean;

    @InjectMocks
    InventoryCountService inventoryCountService;

    @Before
    public void configureMockData(){
        mockStoreBean();
        mockInventoryCountRepository();
        mockInventoryCountProductRepository();
    }

    private void mockInventoryCountRepository(){
        InventoryCount inventoryCount = new InventoryCount();
        inventoryCount.setId(1);
        inventoryCount.setPartialCount(true);
        inventoryCount.setStatus(InventoryCountConstants.PLANNED);

        Mockito.when(inventoryCountRepository.findById(Mockito.anyInt()))
                .thenReturn(Optional.of(inventoryCount));

        Mockito.when(inventoryCountRepository.save(Mockito.any())).thenReturn(null);
    }

    private void mockInventoryCountProductRepository(){
        String sku = "1009";
        String productName = "My Blue Colored Product";
        InventoryCountProduct inventoryCountProduct = new InventoryCountProduct();
        inventoryCountProduct.setSku(sku);
        inventoryCountProduct.setName(productName);

        List<InventoryCountProduct> inventoryCountProductList = new ArrayList<>();
        inventoryCountProductList.add(inventoryCountProduct);

        Mockito.when(inventoryCountProductRepository.findAllByInventoryCountId(Mockito.anyInt()))
                .thenReturn(inventoryCountProductList);

    }

    private void mockStoreBean(){
        Map<String, DetailedProduct> detailedProductMap = new HashMap<>();
        String sku = "1009";
        String productName = "My Blue Colored Product";
        int inventory = 3;

        DetailedProduct detailedProduct = new DetailedProduct(sku, productName);
        detailedProduct.setBigCommerceProduct(ProductGenerator.getBigCommerceProduct(sku, productName,inventory));
        detailedProduct.setVendHQProduct(ProductGenerator.getVendHQProduct(sku, productName,inventory));
        detailedProduct.setAmazonCaProduct(ProductGenerator.getAmazonProduct(sku, productName,inventory));
        detailedProduct.setInventoryLevel(inventory);
        detailedProductMap.put(sku, detailedProduct);

        Mockito.when(storeBean.getDetailedProductsMap()).thenReturn(detailedProductMap);
    }

    @Test
    public void startInventoryCount(){
        InventoryCountRequest icq = inventoryCountService.startInventoryCount(1);
        Assert.assertNotNull(icq);
        Assert.assertEquals(icq.getInventoryCount().getStatus(), InventoryCountConstants.STARTED);
    }

    @Test
    public void abandonInventoryCount(){
        boolean result = inventoryCountService.abandonInventoryCount(1);
        Assert.assertTrue(result);
    }

    @Test
    public void reviewInventoryCount(){
        boolean result = inventoryCountService.reviewInventoryCount(1);
        Assert.assertTrue(result);
    }
}
