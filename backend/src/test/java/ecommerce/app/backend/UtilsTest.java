package ecommerce.app.backend;

import ecommerce.app.backend.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    @Test
    public void testDollarToCents(){
        Long expectedResult = 105L;
        Assert.assertEquals(Utils.dollarToCents("1.05"), expectedResult);
    }

}
