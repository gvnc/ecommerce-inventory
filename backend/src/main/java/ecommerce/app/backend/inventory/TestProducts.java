package ecommerce.app.backend.inventory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

@Component
public class TestProducts {

    @Value("${inventory.update.on.test.skus}")
    private Boolean testEnabled;

    public Set<String> testSkuSet = new HashSet<>();

    public TestProducts(@Value("${inventory.update.test.sku.list}")String testSkuList) {
        StringTokenizer tokenizer = new StringTokenizer(testSkuList, ",");
        while(tokenizer.hasMoreTokens()){
            testSkuSet.add(tokenizer.nextToken());
        }
    }

    public boolean isAvailable(String productSku){
        if(testEnabled == true) {
            return testSkuSet.contains(productSku);
        }
        return true;
    }
}
