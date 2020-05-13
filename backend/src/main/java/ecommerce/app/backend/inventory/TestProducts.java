package ecommerce.app.backend.inventory;

import java.util.HashSet;
import java.util.Set;

public class TestProducts {
    public static Set<String> set = new HashSet();

    static {
        set.add("dummmmyyyyy"); // update nothing for now
        set.add("AR11A103");
        set.add("11089");
        set.add("13202");
        set.add("10502");
        set.add("789625170410");

        //set.add("082045124972");
        //set.add("816204025691");
    }

    public static boolean isAvailable(String productSku){
        return set.contains(productSku);
    }
}
