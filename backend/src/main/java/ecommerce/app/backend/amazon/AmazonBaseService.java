package ecommerce.app.backend.amazon;

import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.*;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersAsyncClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders._2013_09_01.model.*;
import ecommerce.app.backend.amazon.products.AmazonProduct;
import ecommerce.app.backend.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.util.*;

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

    private MarketplaceWebServiceClient mwsClient;
    private MarketplaceWebServiceOrdersClient mwsOrderClient;
    private IdList marketPlaceIdList;
    private List<String> marketPlaceList;

    public AmazonBaseService(Class parentClass, String accessKeyId, String secretAccessKey, String serviceUrl, String merchantId, String marketPlaceId) {
        log = LoggerFactory.getLogger(parentClass);
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.serviceUrl = serviceUrl;
        this.merchantId = merchantId;
        this.marketPlaceId = marketPlaceId;

        initReportClient();
        initOrdersClient();
    }

    private void initReportClient(){
        MarketplaceWebServiceConfig mwsConfig = new MarketplaceWebServiceConfig();
        mwsConfig.setServiceURL(this.serviceUrl);

        this.mwsClient = new MarketplaceWebServiceClient(
                this.accessKeyId, this.secretAccessKey, appName, appVersion, mwsConfig);

        this.marketPlaceIdList = new IdList(Arrays.asList(this.marketPlaceId));
    }

    private void initOrdersClient(){
        MarketplaceWebServiceOrdersConfig config = new MarketplaceWebServiceOrdersConfig();
        config.setServiceURL(this.serviceUrl);

        this.mwsOrderClient = new MarketplaceWebServiceOrdersAsyncClient(this.accessKeyId, this.secretAccessKey,
                this.appName, this.appVersion, config, null);

        this.marketPlaceList = new ArrayList();
        this.marketPlaceList .add(this.marketPlaceId);
    }

    private String reportRequestForProducts() {
        try {
            RequestReportRequest request = new RequestReportRequest()
                    .withMerchant(this.merchantId)
                    .withMarketplaceIdList(this.marketPlaceIdList)
                    .withReportType(this.productListingReportType);

            RequestReportResponse response = this.mwsClient.requestReport(request);
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

            GetReportRequestListResponse response = this.mwsClient.getReportRequestList(request);
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

            GetReportResponse response = this.mwsClient.getReport(request);
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
                try {
                    String productProperties[] = temp.split("\\t");
                    AmazonProduct amazonProduct = new AmazonProduct();
                    amazonProduct.setName(productProperties[0]);
                    amazonProduct.setListingId(productProperties[2]);
                    amazonProduct.setSku(productProperties[3]);

                    if(!productProperties[4].equals(""))
                        amazonProduct.setPrice(Float.parseFloat(productProperties[4]));
                    else
                        amazonProduct.setPrice(0F);

                    if(!productProperties[5].equals(""))
                        amazonProduct.setQuantity(Integer.parseInt(productProperties[5]));
                    else
                        amazonProduct.setQuantity(0);

                    amazonProduct.setId(productProperties[22]);

                    if(productProperties[26].contains("AMAZON")){
                        log.info("Fulfilled by amazon. " + amazonProduct.getSku());
                        amazonProduct.setIsFulfilledByAmazon(true);
                    } else{
                        amazonProduct.setIsFulfilledByAmazon(false);
                    }
                    productList.add(amazonProduct);
                }catch (Exception e){
                    log.warn("Failed to parse product." + temp, e );
                }
            }
            return productList;
        } catch (Exception e) {
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
        final int maxRetryForReportCompletionCheck = 3;
        final int waitTimeInMillisForReportCompletionCheck = 10000;

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

    public List<Order> getOrders(Date date){
        try{
            ListOrdersRequest request = new ListOrdersRequest();
            request.setSellerId(this.merchantId);

            GregorianCalendar gCalendar = new GregorianCalendar();
            gCalendar.setTime(date);

            XMLGregorianCalendar lastUpdated = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
            request.setLastUpdatedAfter(lastUpdated);

            request.setMarketplaceId(this.marketPlaceList);

            request.setMaxResultsPerPage(10);

            ListOrdersResponse response = this.mwsOrderClient.listOrders(request);

            ListOrdersResult listOrdersResult = response.getListOrdersResult();
            if(listOrdersResult != null){
                return listOrdersResult.getOrders();
            }
        } catch (Exception e){
            if(e instanceof MarketplaceWebServiceException){
                MarketplaceWebServiceException mwe = (MarketplaceWebServiceException) e;
                if(mwe.getErrorCode().equals("RequestThrottled"))
                    log.error("Failed to get orders, request is throttled.");
            } else {
                log.error("Failed to get orders", e);
            }
        }
        return null;
    }

    public List<OrderItem> getOrderItems(String orderId){
        try{
            ListOrderItemsRequest request = new ListOrderItemsRequest();
            request.setSellerId(this.merchantId);
            request.setAmazonOrderId(orderId);

            ListOrderItemsResponse response = this.mwsOrderClient.listOrderItems(request);
            ListOrderItemsResult listOrderItemsResult = response.getListOrderItemsResult();
            if(listOrderItemsResult != null){
                return listOrderItemsResult.getOrderItems();
            }
        } catch (Exception e){
            log.error("Failed to get order items.", e);
        }
        return null;
    }

    private void sleepFor(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        }catch (Exception e){ }
    }

    private String getFeedHeader(String updateType){
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<AmazonEnvelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"amzn-envelope.xsd\">" +
                "<Header>" +
                "<DocumentVersion>1.02</DocumentVersion>" +
                "<MerchantIdentifier>" + this.merchantId + "</MerchantIdentifier>" +
                "</Header>" +
                "<MessageType>" + updateType + "</MessageType>";
    }

    private String submitFeed(String feedXml, String feedType){
        log.info("Submit feed initiated.");
        log.info("Submit Feed XML " + feedXml);
        int maxRetry = 5;
        long sleepBeforeRetry = 30000;
        for(int i=0; i<maxRetry; i++) {
            try{
                SubmitFeedRequest request = new SubmitFeedRequest();
                request.setMerchant(merchantId);
                request.setMarketplaceIdList(this.marketPlaceIdList);
                request.setPurgeAndReplace(false);
                request.setFeedType(feedType);
                request.setContentType(ContentType.TextXml);

                InputStream feedStream = new ByteArrayInputStream(feedXml.getBytes());
                request.setFeedContent(feedStream);
                request.setContentMD5(Utils.computeContentMD5Header(feedXml));

                SubmitFeedResponse response = this.mwsClient.submitFeed(request);
                if (response.isSetSubmitFeedResult()) {
                    SubmitFeedResult submitFeedResult = response.getSubmitFeedResult();
                    if (submitFeedResult.isSetFeedSubmissionInfo()) {
                        FeedSubmissionInfo feedSubmissionInfo = submitFeedResult.getFeedSubmissionInfo();
                        if (feedSubmissionInfo.isSetFeedSubmissionId()) {
                            log.info("FeedSubmissionId is " + feedSubmissionInfo.getFeedSubmissionId());
                            return feedSubmissionInfo.getFeedSubmissionId();
                        }
                    }
                }
            } catch (Exception e){
                if(e instanceof MarketplaceWebServiceException){
                    MarketplaceWebServiceException mwe = (MarketplaceWebServiceException) e;
                    if(mwe.getErrorCode().equals("RequestThrottled"))
                        log.warn("Failed to submit feed, request is throttled.");
                } else {
                    log.error("Failed to submit feed.", e);
                }
            }
            if(i < maxRetry-1){
                log.warn("Sleep for " + sleepBeforeRetry + " before retry to get submit feed.");
                this.sleepFor(sleepBeforeRetry);
            }
        }
        log.error("Failed to submit feed after retries.");
        return null;
    }

    private boolean isFeedSubmissionSuccessful(String feedSubmissionId){
        int maxRetry = 4;
        long sleepBeforeRetry = 45000;
        for(int i=0; i<maxRetry; i++) {
            try {
                log.warn("Sleep for " + sleepBeforeRetry + " before retry to get feed submission result. [feedSubmissionId:" + feedSubmissionId + "]");
                this.sleepFor(sleepBeforeRetry);

                GetFeedSubmissionResultRequest request = new GetFeedSubmissionResultRequest();
                request.setMerchant(this.merchantId);
                request.setFeedSubmissionId(feedSubmissionId);

                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                request.setFeedSubmissionResultOutputStream(responseStream);

                this.mwsClient.getFeedSubmissionResult(request);
                String responseXml = responseStream.toString("UTF-8");

                if(responseXml.contains("<StatusCode>Complete</StatusCode>")){
                    log.info("Feed Submission Response " + responseXml);
                    log.info("Feed submission is completed. [feedSubmissionId:" + feedSubmissionId + "]");
                    return true;
                }
            }catch (Exception e){
                if(e instanceof MarketplaceWebServiceException){
                    MarketplaceWebServiceException mwe = (MarketplaceWebServiceException) e;
                    if(mwe.getErrorCode().equals("FeedProcessingResultNotReady"))
                        log.warn("Feed processing result not ready yet. [feedSubmissionId:" + feedSubmissionId + "]");
                } else {
                    log.error("Failed to get feed submission result.", e);
                }
            }
        }
        log.error("Failed to get submission result after retries. [feedSubmissionId:" + feedSubmissionId + "]");
        return false;
    }

    public boolean updateProductQuantity(List<AmazonProduct> productList){
        log.info("Product quantity update initiated.");
        try{
            if(productList.size() == 0){
                log.warn("No products found to update.");
                return true;
            }

            String feedHeader = getFeedHeader("Inventory");
            StringBuffer feedMessages = new StringBuffer();
            int i = 1;
            for(AmazonProduct amazonProduct:productList){
                feedMessages.append(
                        "<Message>" +
                        "  <MessageID>" + i + "</MessageID>" +
                        "  <OperationType>Update</OperationType>" +
                        "  <Inventory>" +
                        "    <SKU>" + amazonProduct.getSku() + "</SKU>" +
                        "    <Quantity>" + amazonProduct.getQuantity() +"</Quantity>" +
                        "  </Inventory>" +
                        "</Message>"
                );
                i++;
            }

            String feedXml = feedHeader + feedMessages + "</AmazonEnvelope>";

            String feedSubmissionId = submitFeed(feedXml, "_POST_INVENTORY_AVAILABILITY_DATA_");
            return isFeedSubmissionSuccessful(feedSubmissionId);
        } catch (Exception e){
            log.error("Failed to update product quantity.", e);
        }
        return false;
    }

    public boolean updateProductPrice(List<AmazonProduct> productList){
        log.info("Product price update initiated.");
        try{
            if(productList.size() == 0){
                log.warn("No products found to update.");
                return true;
            }

            String feedHeader = getFeedHeader("Price");
            StringBuffer feedMessages = new StringBuffer();
            int i = 1;
            for(AmazonProduct amazonProduct:productList){
                feedMessages.append(
                        "<Message>" +
                        "  <MessageID>" + i + "</MessageID>" +
                        "  <Price>" +
                        "    <SKU>" + amazonProduct.getSku() + "</SKU>" +
                        "    <StandardPrice currency=\"CAD\">" + amazonProduct.getPrice() +"</StandardPrice>" +
                        "  </Price>" +
                        "</Message>"
                );
                i++;
            }

            String feedXml = feedHeader + feedMessages + "</AmazonEnvelope>";
            String feedSubmissionId = submitFeed(feedXml, "_POST_PRODUCT_PRICING_DATA_");
            return isFeedSubmissionSuccessful(feedSubmissionId);
        } catch (Exception e){
            log.error("Failed to update product prizes.", e);
        }
        return false;
    }
}