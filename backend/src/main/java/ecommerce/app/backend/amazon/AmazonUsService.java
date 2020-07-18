package ecommerce.app.backend.amazon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AmazonUsService extends AmazonBaseService {

    public AmazonUsService(@Value("${aws.us.accessKeyId}")String accessKeyId,
                           @Value("${aws.us.secret}")String secretAccessKey,
                           @Value("${aws.us.serviceUrl}")String serviceUrl,
                           @Value("${aws.us.merchantId}")String merchantId,
                           @Value("${aws.ca.marketPlaceId}")String marketPlaceId) {
        super(AmazonUsService.class, accessKeyId, secretAccessKey, serviceUrl, merchantId, marketPlaceId);
    }
}
