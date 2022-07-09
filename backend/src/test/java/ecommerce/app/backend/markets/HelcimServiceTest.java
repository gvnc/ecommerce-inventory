package ecommerce.app.backend.markets;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.inventory.TestProducts;
import ecommerce.app.backend.markets.helcim.HelcimAPIService;
import ecommerce.app.backend.markets.helcim.products.HelcimOrder;
import ecommerce.app.backend.markets.helcim.products.HelcimOrderItem;
import ecommerce.app.backend.markets.helcim.products.HelcimProduct;
import ecommerce.app.backend.model.BaseProduct;
import ecommerce.app.backend.model.DetailedProduct;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
public class HelcimServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8900);

    private HelcimAPIService helcimAPIService;

    private StoreBean storeBean = new StoreBean();

    private HelcimProduct testProduct;
    private final String testSku = "849176011010";

    private void initStoreBean(){
        BaseProduct baseProduct = new BaseProduct(testSku, "First Strike T15 Paintball Gun");
        baseProduct.setHelcimPrice(120.0F);
        baseProduct.setHelcimInventory(2);
        storeBean.getProductsMap().put(testSku, baseProduct);

        testProduct = new HelcimProduct();
        testProduct.setId(1067954);
        testProduct.setStock(2.00F);
        testProduct.setSku(testSku);
        testProduct.setName("First Strike T15 Paintball Gun");

        DetailedProduct detailedProduct = new DetailedProduct(testSku, "First Strike X");
        detailedProduct.setHelcimProduct(testProduct);
        storeBean.getDetailedProductsMap().put(testSku, detailedProduct);
    }

    @Before
    public void configureStub(){
        initStoreBean();
        helcimAPIService = new HelcimAPIService("http://localhost:8900/helcim", "testing", "", true);
       // helcimAPIService = new HelcimAPIService("https://secure.myhelcim.com/api", "b9RtH9wBaQnT64gh2HK5dcD3s", "2500270978", true);
        ReflectionTestUtils.setField(helcimAPIService, "storeBean", storeBean);
        TestProducts testProducts = new TestProducts(testSku);
        ReflectionTestUtils.setField(helcimAPIService, "testProducts", testProducts);

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/helcim/product/search"))
                .willReturn(WireMock.ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                        .withBodyFile("helcimProductsResponse.xml")));

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/helcim/product/inventory-update"))
                        .willReturn(WireMock.ok()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                                .withBodyFile("helcimProductUpdateResponse.xml")));

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/helcim/product/modify"))
                        .willReturn(WireMock.ok()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                                .withBodyFile("helcimProductUpdateResponse.xml")));


        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/helcim/invoice/search"))
                        .willReturn(WireMock.ok()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                                .withBodyFile("helcimOrderListResponse.xml")));


        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/helcim/invoice/view"))
                        .willReturn(WireMock.ok()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                                .withBodyFile("helcimOrderDetailResponse.xml")));
    }

    @Test
    public void getProductList(){
        List<HelcimProduct> helcimProductList = helcimAPIService.getProductList();
        Assert.assertNotNull(helcimProductList);
        Assert.assertTrue(helcimProductList.size() > 0);
    }

    @Test
    public void updateInventory(){
        boolean updateResult = helcimAPIService.updateInventory(testProduct, 2);
        Assert.assertTrue(updateResult);
    }

    @Test
    public void updatePrice(){
        boolean updateResult = helcimAPIService.updatePrice(testSku, "3.00");
        Assert.assertTrue(updateResult);
    }

    @Test
    public void getOrders(){
        List<HelcimOrder> helcimOrderList = helcimAPIService.getOrders();
        Assert.assertNotNull(helcimOrderList);
        Assert.assertTrue(helcimOrderList.size()>0);
    }

    @Test
    public void getOrderProducts(){
        List<HelcimOrderItem> helcimOrderItemList = helcimAPIService.getOrderProducts("234208");
        Assert.assertNotNull(helcimOrderItemList);
        Assert.assertTrue(helcimOrderItemList.size()>0);
    }

}
