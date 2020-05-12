package ecommece.app.backend;

import ecommece.app.backend.bigcommerce.BigCommerceAPIService;
import ecommece.app.backend.inventory.OrderListener;
import ecommece.app.backend.sync.SyncProductsService;
import ecommece.app.backend.vendhq.VendHQAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner  implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AppStartupRunner.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SyncProductsService syncProductsService;

    @Autowired
    private OrderListener orderListener;

    @Autowired
    private BigCommerceAPIService bigCommerceAPIService;

    @Autowired
    private VendHQAPIService vendhqApiService;

    @Override
    public void run(String...args) throws Exception {
        logger.info("Application started and ready.");
        //bigCommerceAPIService.updateProductQuantity("789625170410", 0, false);
        //vendhqApiService.updateProductQuantity("789625170410", 0, true);
        syncProductsService.syncAllMarketPlaces();
        //String newPassword = passwordEncoder.encode("garner1!");
        //logger.info("password is " + newPassword);
    }
}