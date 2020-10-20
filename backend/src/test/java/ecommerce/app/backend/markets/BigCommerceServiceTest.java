package ecommerce.app.backend.markets;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.order.BCOrder;
import ecommerce.app.backend.markets.bigcommerce.order.BCOrderProduct;
import ecommerce.app.backend.markets.bigcommerce.products.BigCommerceProduct;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
public class BigCommerceServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8900);

    private BigCommerceAPIService bigCommerceAPIService;

    private StoreBean storeBean = new StoreBean();

    @Before
    public void configureStub(){
        bigCommerceAPIService = new BigCommerceAPIService("http://localhost:8900/bigcommerce", "testing", "testing");
        ReflectionTestUtils.setField(bigCommerceAPIService, "storeBean", storeBean);

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/bigcommerce/v3/catalog/products"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", "application/json;charset=utf-8")
                        .withBodyFile("bcProductListResponse.json")));

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/bigcommerce/v2/orders"))
                        .willReturn(WireMock.ok()
                                .withHeader("Content-Type", "application/xml;charset=utf-8")
                                .withBodyFile("bcOrdersResponse.xml")));

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/bigcommerce/v2/orders/10035/products"))
                        .willReturn(WireMock.ok()
                                .withHeader("Content-Type", "application/xml;charset=utf-8")
                                .withBodyFile("bcOrderProductsResponse.xml")));
    }

    @Test
    public void getProductList(){
        BigCommerceProduct[] productList = bigCommerceAPIService.getProductList(1);
        Assert.assertNotNull(productList);
        Assert.assertTrue(productList.length > 0);
    }

    @Test
    public void getOrders(){
        List<BCOrder> orders = bigCommerceAPIService.getOrders();
        Assert.assertNotNull(orders);
        Assert.assertTrue(orders.size() > 0);
    }

    @Test
    public void getOrderProducts(){
        List<BCOrderProduct> orderProducts = bigCommerceAPIService.getOrderProducts("10035");
        Assert.assertNotNull(orderProducts);
        Assert.assertTrue(orderProducts.size() > 0);
    }
}
