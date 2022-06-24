package ecommerce.app.backend.markets;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.helcim.HelcimAPIService;
import ecommerce.app.backend.markets.helcim.products.HelcimProduct;
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

    @Before
    public void configureStub(){
        helcimAPIService = new HelcimAPIService("http://localhost:8900/helcim", "testing", "");
       // helcimAPIService = new HelcimAPIService("https://secure.myhelcim.com/api", "b9RtH9wBaQnT64gh2HK5dcD3s", "2500270978");
        ReflectionTestUtils.setField(helcimAPIService, "storeBean", storeBean);

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
    }

    @Test
    public void getProductList(){
        List<HelcimProduct> helcimProductList = helcimAPIService.getProductList();
        Assert.assertNotNull(helcimProductList);
        Assert.assertTrue(helcimProductList.size() > 0);
    }

    @Test
    public void updateInventory(){
        String sku = "12345";
        HelcimProduct helcimProduct = new HelcimProduct();
        helcimProduct.setId(1067954L);
        helcimProduct.setStock(5.00F);
        helcimProduct.setSku(sku);

        boolean updateResult = helcimAPIService.updateInventory(helcimProduct, 2);
        Assert.assertTrue(updateResult);
    }
}
