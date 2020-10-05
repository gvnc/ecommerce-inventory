package ecommerce.app.backend.markets;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class BigCommerceServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8900);

    private BigCommerceAPIService bigCommerceAPIService;

    @Before
    public void configureStub(){
        bigCommerceAPIService = new BigCommerceAPIService("http://localhost:8900/bigcommerce", "testing", "testing");
        ReflectionTestUtils.setField(bigCommerceAPIService, "storeBean", new StoreBean());
        wireMockRule.stubFor(WireMock.any(WireMock.urlPathEqualTo("/bigcommerce/v3/catalog/products"))
                .willReturn(WireMock.aResponse()));
    }

    @Test
    public void testSoemthing(){
        bigCommerceAPIService.getProductList(1);
    }
}
