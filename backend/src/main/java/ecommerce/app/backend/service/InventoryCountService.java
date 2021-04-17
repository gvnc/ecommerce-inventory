package ecommerce.app.backend.service;

import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.markets.amazon.AmazonCaService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceAPIService;
import ecommerce.app.backend.markets.bigcommerce.BigCommerceFSAPIService;
import ecommerce.app.backend.markets.vendhq.VendHQAPIService;
import ecommerce.app.backend.service.constants.InventoryCountConstants;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.model.InventoryCountRequest;
import ecommerce.app.backend.repository.InventoryCountProductRepository;
import ecommerce.app.backend.repository.InventoryCountRepository;
import ecommerce.app.backend.repository.model.InventoryCount;
import ecommerce.app.backend.repository.model.InventoryCountProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class InventoryCountService {

    @Autowired
    private InventoryCountRepository inventoryCountRepository;

    @Autowired
    private InventoryCountProductRepository inventoryCountProductRepository;

    @Autowired
    private BigCommerceAPIService bigCommerceAPIService;

    @Autowired
    private BigCommerceFSAPIService bigCommerceFSAPIService;

    @Autowired
    private VendHQAPIService vendHQAPIService;

    @Autowired
    private AmazonCaService amazonCaService;

    @Autowired
    private StoreBean storeBean;

    public List<InventoryCount> getInventoryCounts() {
        return inventoryCountRepository.findAllByOrderByIdDesc();
    }

    public InventoryCountRequest getInventoryCountById(Integer id) {
        try{
            InventoryCountRequest icq = new InventoryCountRequest();
            InventoryCount inventoryCount = inventoryCountRepository.findById(id).orElse(null);
            icq.setInventoryCount(inventoryCount);

            List<InventoryCountProduct> productList = inventoryCountProductRepository.findAllByInventoryCountId(id);
            icq.setProductList(productList);

            return icq;
        } catch (Exception e){
            log.error("Failed to get inventory count by id " + id, e);
        }
        return null;
    }

    @Transactional
    public InventoryCountRequest saveOrUpdateInventoryCount(InventoryCountRequest inventoryCountRequest) {
        try{
            InventoryCount inventoryCount = inventoryCountRequest.getInventoryCount();
            if(inventoryCount.getId() == null){
                inventoryCount.setCreateDate(new Date());
                inventoryCount.setStatus(InventoryCountConstants.PLANNED);
            }
            inventoryCountRepository.save(inventoryCount);
            inventoryCountRequest.setInventoryCount(inventoryCount);

            // delete current products
            inventoryCountProductRepository.deleteByInventoryCountId(inventoryCount.getId());

            if(inventoryCount.getPartialCount() == true) {
                List<InventoryCountProduct> products = inventoryCountRequest.getProductList();
                if (products != null && products.size() > 0) {
                    products.stream().forEach(product -> product.setInventoryCountId(inventoryCount.getId()));
                    inventoryCountProductRepository.saveAll(products);
                }
            }
            return inventoryCountRequest;
        } catch (Exception e){
            log.error("Failed to save inventory count.", e);
            return null;
        }
    }

    public InventoryCountRequest startInventoryCount(Integer id) {
        try{
            InventoryCountRequest inventoryCountRequest = new InventoryCountRequest();

            // change inventory count status
            InventoryCount inventoryCount = inventoryCountRepository.findById(id).orElse(null);
            if(inventoryCount != null){
                inventoryCountRequest.setInventoryCount(inventoryCount);
                inventoryCount.setStatus(InventoryCountConstants.STARTED);
                inventoryCountRepository.save(inventoryCount);

                // find products and set inventory levels
                List<InventoryCountProduct> productList;
                if(inventoryCount.getPartialCount() == true){
                    productList = inventoryCountProductRepository.findAllByInventoryCountId(id);
                    productList.stream().forEach(product -> {
                        DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(product.getSku());
                        if(detailedProduct.getVendHQProduct() != null){
                            if(detailedProduct.getVendHQProduct().getInventory() != null)
                                product.setVendhqQuantity(detailedProduct.getVendHQProduct().getInventory().getInventoryLevel());
                        }
                        if(detailedProduct.getBigCommerceProduct() != null){
                            product.setBigcommerceQuantity(detailedProduct.getBigCommerceProduct().getInventoryLevel());
                        }
                        if(detailedProduct.getBigCommerceFSProduct() != null){
                            product.setBigcommerceFSQuantity(detailedProduct.getBigCommerceFSProduct().getInventoryLevel());
                        }
                        if(detailedProduct.getAmazonCaProduct() != null){
                            product.setAmazonCAQuantity(detailedProduct.getAmazonCaProduct().getQuantity());
                        }
                    });
                } else { // full count detected
                    productList = new ArrayList<>();
                    storeBean.getDetailedProductsMap().forEach((sku, detailedProduct) -> {
                        InventoryCountProduct product = new InventoryCountProduct();
                        product.setInventoryCountId(inventoryCount.getId());
                        product.setSku(detailedProduct.getSku());
                        product.setName(detailedProduct.getName());
                        if(detailedProduct.getVendHQProduct() != null){
                            if(detailedProduct.getVendHQProduct().getInventory() != null)
                                product.setVendhqQuantity(detailedProduct.getVendHQProduct().getInventory().getInventoryLevel());
                        }
                        if(detailedProduct.getBigCommerceProduct() != null){
                            product.setBigcommerceQuantity(detailedProduct.getBigCommerceProduct().getInventoryLevel());
                        }
                        if(detailedProduct.getBigCommerceFSProduct() != null){
                            product.setBigcommerceQuantity(detailedProduct.getBigCommerceFSProduct().getInventoryLevel());
                        }
                        if(detailedProduct.getAmazonCaProduct() != null){
                            product.setBigcommerceQuantity(detailedProduct.getAmazonCaProduct().getQuantity());
                        }
                        productList.add(product);
                    });
                }
                inventoryCountProductRepository.saveAll(productList);
                inventoryCountRequest.setProductList(productList);
                return inventoryCountRequest;
            }
        } catch (Exception e){
            log.error("Failed to save inventory count.", e);
        }
        return null;
    }

    public boolean saveInventoryCountProduct(InventoryCountProduct inventoryCountProduct) {
        try{
            inventoryCountProductRepository.save(inventoryCountProduct);
            return true;
        } catch (Exception e){
            log.error("Failed to save inventory count product.", e);
        }
        return false;
    }

    public boolean abandonInventoryCount(Integer id) {
        try{
            // change inventory count status
            InventoryCount inventoryCount = inventoryCountRepository.findById(id).orElse(null);
            if(inventoryCount != null){
                inventoryCount.setStatus(InventoryCountConstants.ABANDONED);
                inventoryCountRepository.save(inventoryCount);
                return true;
            }
        } catch (Exception e){
            log.error("Failed to save inventory count.", e);
        }
        return false;
    }

    public boolean reviewInventoryCount(Integer id) {
        try{
            // change inventory count status
            InventoryCount inventoryCount = inventoryCountRepository.findById(id).orElse(null);
            if(inventoryCount != null){
                inventoryCount.setStatus(InventoryCountConstants.REVIEW);
                inventoryCountRepository.save(inventoryCount);
                return true;
            }
        } catch (Exception e){
            log.error("Failed to save inventory count.", e);
        }
        return false;
    }

    public boolean startUpdateInventories(Integer id) {
        try{
            // change inventory count status
            InventoryCount inventoryCount = inventoryCountRepository.findById(id).orElse(null);
            if(inventoryCount != null){
                inventoryCount.setStatus(InventoryCountConstants.INVENTORY_UPDATE_INPROGRESS);
                inventoryCountRepository.save(inventoryCount);

                List<InventoryCountProduct> inventoryCountProductList = inventoryCountProductRepository.findAllByInventoryCountIdAndCountedAndMatched(id, true, false);
                for(InventoryCountProduct inventoryCountProduct:inventoryCountProductList){
                    DetailedProduct detailedProduct = storeBean.getDetailedProductsMap().get(inventoryCountProduct.getSku());
                    detailedProduct.setInventoryLevel(inventoryCountProduct.getCount());
                    vendHQAPIService.updateProductQuantity(detailedProduct.getVendHQProduct(), inventoryCountProduct.getSku(), inventoryCountProduct.getCount(), true);
                    bigCommerceAPIService.updateProductQuantity(detailedProduct.getBigCommerceProduct(), inventoryCountProduct.getSku(), inventoryCountProduct.getCount(), true);
                    bigCommerceFSAPIService.updateProductQuantity(detailedProduct.getBigCommerceFSProduct(), inventoryCountProduct.getSku(), inventoryCountProduct.getCount(), true);
                    amazonCaService.updateInventory(inventoryCountProduct.getSku(), inventoryCountProduct.getCount(), true);
                }
                inventoryCount.setStatus(InventoryCountConstants.INVENTORY_UPDATE_COMPLETED);
                inventoryCountRepository.save(inventoryCount);
                return true;
            }
        } catch (Exception e){
            log.error("Failed to do update for inventory count id {}.", id, e);
        }
        return false;
    }
}
