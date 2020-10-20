package ecommerce.app.backend.markets;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.markets.vendhq.products.VendHQInventoryData;
import ecommerce.app.backend.markets.vendhq.products.VendHQProductsData;
import ecommerce.app.backend.markets.vendhq.sales.VendHQSale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
public class VendHQServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8900);

    private VendHQAPIService vendHQAPIService;

    private StoreBean storeBean = new StoreBean();

    @Before
    public void configureStub(){
        vendHQAPIService = new VendHQAPIService("http://localhost:8900/vendhq", "testing");
        ReflectionTestUtils.setField(vendHQAPIService, "storeBean", storeBean);

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/vendhq/2.0/products"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", "application/json;charset=utf-8")
                        .withBodyFile("vendhqProductListResponse.json")));

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/vendhq/2.0/inventory"))
                        .willReturn(WireMock.ok()
                                .withHeader("Content-Type", "application/json;charset=utf-8")
                                .withBodyFile("vendhqInventoryListResponse.json")));

        wireMockRule
                .stubFor(WireMock.any(WireMock.urlPathEqualTo("/vendhq/2.0/sales"))
                        .willReturn(WireMock.ok()
                                .withHeader("Content-Type", "application/json;charset=utf-8")
                                .withBodyFile("vendhqSalesResponse.json")));
    }

    @Test
    public void getProductList(){
        VendHQProductsData vendHQProductsData = vendHQAPIService.getProductList(0);
        Assert.assertNotNull(vendHQProductsData);
        Assert.assertNotNull(vendHQProductsData.getData());
        Assert.assertTrue(vendHQProductsData.getData().length > 0);
    }

    @Test
    public void getInventoryList(){
        VendHQInventoryData vendHQInventoryData = vendHQAPIService.getInventoryList(0);
        Assert.assertNotNull(vendHQInventoryData);
        Assert.assertNotNull(vendHQInventoryData.getData());
        Assert.assertTrue(vendHQInventoryData.getData().length > 0);
    }

    @Test
    public void getSales(){
        List<VendHQSale> saleList = vendHQAPIService.getSales(0L);
        Assert.assertNotNull(saleList);
        Assert.assertTrue(saleList.size() > 0);
    }
}
