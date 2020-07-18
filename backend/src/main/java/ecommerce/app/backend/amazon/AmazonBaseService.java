package ecommerce.app.backend.amazon;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.model.*;
import ecommerce.app.backend.amazon.products.AmazonProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmazonBaseService {

    private static Logger log;

    private String appName = "GarnersApp";
    private String appVersion = "0.0.1";

    private String accessKeyId = null;
    private String secretAccessKey = null;
    private String serviceUrl = null;
    private String merchantId = null;
    private String marketPlaceId = null;

    private final String productListingReportType = "_GET_MERCHANT_LISTINGS_DATA_";

    private MarketplaceWebServiceConfig mwsConfig;
    private MarketplaceWebService mwsService;
    private IdList marketPlaceList;

    public AmazonBaseService(Class parentClass, String accessKeyId, String secretAccessKey, String serviceUrl, String merchantId, String marketPlaceId) {
        log = LoggerFactory.getLogger(parentClass);
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.serviceUrl = serviceUrl;
        this.merchantId = merchantId;
        this.marketPlaceId = marketPlaceId;

        this.mwsConfig = new MarketplaceWebServiceConfig();
        mwsConfig.setServiceURL(this.serviceUrl);

        mwsService = new MarketplaceWebServiceClient(
                this.accessKeyId, this.secretAccessKey, appName, appVersion, this.mwsConfig);

        marketPlaceList = new IdList(Arrays.asList(this.marketPlaceId));
    }

    private String reportRequestForProducts() {
        try {
            RequestReportRequest request = new RequestReportRequest()
                    .withMerchant(this.merchantId)
                    .withMarketplaceIdList(this.marketPlaceList)
                    .withReportType(this.productListingReportType);

            RequestReportResponse response = this.mwsService.requestReport(request);
            if (response.isSetRequestReportResult()) {
                RequestReportResult requestReportResult = response.getRequestReportResult();
                if(requestReportResult.isSetReportRequestInfo()){
                    ReportRequestInfo reportRequestInfo = requestReportResult.getReportRequestInfo();
                    if (reportRequestInfo.isSetReportRequestId()) {
                        return reportRequestInfo.getReportRequestId();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to request report for " + this.productListingReportType, e);
        }
        return null;
    }

    private ReportRequestInfo getReportRequestInfo(String reportRequestId){
        try {
            GetReportRequestListRequest request = new GetReportRequestListRequest();
            request.setMerchant( merchantId );
            request.setReportRequestIdList(new IdList(Arrays.asList(reportRequestId)));

            GetReportRequestListResponse response = this.mwsService.getReportRequestList(request);
            if (response.isSetGetReportRequestListResult()) {
                GetReportRequestListResult  getReportRequestListResult = response.getGetReportRequestListResult();
                List<ReportRequestInfo> reportRequestInfoList = getReportRequestListResult.getReportRequestInfoList();
                return reportRequestInfoList.get(0);
            }
        } catch (Exception e){
            log.error("Failed to get report request status for " + reportRequestId, e);
        }
        return null;
    }

    private OutputStream getReportOutputStream(String reportId){
        try {
            GetReportRequest request = new GetReportRequest();
            request.setMerchant( merchantId );
            request.setReportId(reportId);
            request.setReportOutputStream(new ByteArrayOutputStream());

            GetReportResponse response = this.mwsService.getReport(request);
            if(response.isSetGetReportResult()){
                return request.getReportOutputStream();
            }
        }catch (Exception e){
            log.error("Failed to get report outputstream.", e);
        }
        return null;
    }

    private List<AmazonProduct> convertToProducts(OutputStream outputStream){

        List productList = new ArrayList();

        byte content [] = ((ByteArrayOutputStream)outputStream).toByteArray();
        InputStream is = null;
        BufferedReader bfReader = null;
        try {
            is = new ByteArrayInputStream(content);
            bfReader = new BufferedReader(new InputStreamReader(is));
            String temp = bfReader.readLine(); // first line is header, skip it
            while((temp = bfReader.readLine()) != null){
                String productProperties[] = temp.split("\\t");
                AmazonProduct amazonProduct = new AmazonProduct();
                amazonProduct.setName(productProperties[0]);
                amazonProduct.setListingId(productProperties[2]);
                amazonProduct.setSku(productProperties[3]);
                amazonProduct.setPrice(Float.parseFloat(productProperties[4]));
                amazonProduct.setQuantity(Integer.parseInt(productProperties[5]));
                amazonProduct.setId(productProperties[22]);
                productList.add(amazonProduct);
            }
            return productList;
        } catch (IOException e) {
            log.error("Failed to parse report output stream to products.", e);
            return null;
        } finally {
            try{
                if(is != null) is.close();
            } catch (Exception ex){ }
        }
    }

    public List<AmazonProduct> getProductList(){
        final String statusDone = "_DONE_";
        final int maxRetryForReportCompletionCheck = 5;
        final int waitTimeInMillisForReportCompletionCheck = 5000;

        try {
            String reportRequestId = reportRequestForProducts();
            if(reportRequestId != null){
                for(int i=0; i<maxRetryForReportCompletionCheck; i++){
                    Thread.sleep(waitTimeInMillisForReportCompletionCheck);
                    ReportRequestInfo reportRequestInfo = getReportRequestInfo(reportRequestId);
                    if(reportRequestInfo.getReportProcessingStatus().equals(statusDone)){
                        String generatedReportId = reportRequestInfo.getGeneratedReportId();
                        OutputStream reportOutputStream = getReportOutputStream(generatedReportId);
                        return convertToProducts(reportOutputStream);
                    }
                }
                log.error("Failed to get product list, report status never gets DONE in configured time.");
            } else {
                log.error("Failed to get product list, report request id is null.");
            }
        } catch (Exception e){
            log.error("Failed to get product list.", e);
        }
        return null;
    }
}