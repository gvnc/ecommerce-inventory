package ecommerce.app.backend.amazon;

import ecommerce.app.backend.StoreBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AmazonCaService extends AmazonBaseService {

    @Autowired
    private StoreBean storeBean;

    public AmazonCaService(@Value("${aws.ca.accessKeyId}")String accessKeyId,
                           @Value("${aws.ca.secret}")String secretAccessKey,
                           @Value("${aws.ca.serviceUrl}")String serviceUrl,
                           @Value("${aws.ca.merchantId}")String merchantId,
                           @Value("${aws.ca.marketPlaceId}")String marketPlaceId) {
        super(AmazonCaService.class, accessKeyId, secretAccessKey, serviceUrl, merchantId, marketPlaceId);
    }
}
