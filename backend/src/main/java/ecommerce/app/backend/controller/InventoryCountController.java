package ecommerce.app.backend.controller;


import ecommerce.app.backend.StoreBean;
import ecommerce.app.backend.model.DetailedProduct;
import ecommerce.app.backend.model.InventoryCountRequest;
import ecommerce.app.backend.repository.InventoryCountProductRepository;
import ecommerce.app.backend.repository.InventoryCountRepository;
import ecommerce.app.backend.repository.model.InventoryCount;
import ecommerce.app.backend.repository.model.InventoryCountProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = { "http://95.111.250.92:3000", "http://localhost:3000", "http://localhost:4200" })
@RequestMapping("/inventoryCount")
public class InventoryCountController {

    @Autowired
    private InventoryCountRepository inventoryCountRepository;

    @Autowired
    private InventoryCountProductRepository inventoryCountProductRepository;

    @Autowired
    private StoreBean storeBean;

    @GetMapping("/list")
    public List<InventoryCount> getInventoryCounts() {
        List<InventoryCount> inventoryCountList = new ArrayList<>();
        try{
            Iterable<InventoryCount> iterable = inventoryCountRepository.findAllByOrderByIdDesc();
            iterable.forEach(inventoryCountList::add);
        } catch (Exception e){
            log.error("Failed to get purchase orders.", e);
        }
        return  inventoryCountList;
    }

    @GetMapping("/get/{id}")
    public InventoryCountRequest getInventoryCountById(@PathVariable Integer id) {
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
    @PostMapping("/saveOrUpdate")
    public InventoryCountRequest saveOrUpdateInventoryCount(@RequestBody InventoryCountRequest inventoryCountRequest) {
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

    @PostMapping("/start/{id}")
    public InventoryCountRequest startInventoryCount(@PathVariable Integer id) {
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

    @PostMapping("/saveInventoryCountProduct")
    public String saveInventoryCountProduct(@RequestBody InventoryCountProduct inventoryCountProduct) {
        try{
            inventoryCountProductRepository.save(inventoryCountProduct);
            return OperationConstants.SUCCESS;
        } catch (Exception e){
            log.error("Failed to save inventory count product.", e);
        }
        return OperationConstants.FAIL;
    }

    @PostMapping("/abandon/{id}")
    public String abandonInventoryCount(@PathVariable Integer id) {
        try{
            // change inventory count status
            InventoryCount inventoryCount = inventoryCountRepository.findById(id).orElse(null);
            if(inventoryCount != null){
                inventoryCount.setStatus(InventoryCountConstants.ABANDONED);
                inventoryCountRepository.save(inventoryCount);
                return OperationConstants.SUCCESS;
            }
        } catch (Exception e){
            log.error("Failed to save inventory count.", e);
        }
        return OperationConstants.FAIL;
    }

    @PostMapping("/review/{id}")
    public String reviewInventoryCount(@PathVariable Integer id) {
        try{
            // change inventory count status
            InventoryCount inventoryCount = inventoryCountRepository.findById(id).orElse(null);
            if(inventoryCount != null){
                inventoryCount.setStatus(InventoryCountConstants.REVIEW);
                inventoryCountRepository.save(inventoryCount);
                return OperationConstants.SUCCESS;
            }
        } catch (Exception e){
            log.error("Failed to save inventory count.", e);
        }
        return OperationConstants.FAIL;
    }
}
