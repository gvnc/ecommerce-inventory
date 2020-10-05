package ecommerce.app.backend.controller;


import ecommerce.app.backend.model.InventoryCountRequest;
import ecommerce.app.backend.repository.model.InventoryCount;
import ecommerce.app.backend.repository.model.InventoryCountProduct;
import ecommerce.app.backend.service.InventoryCountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = { "http://95.111.250.92:3000", "http://localhost:3000", "http://localhost:4200" })
@RequestMapping("/inventoryCount")
public class InventoryCountController {

    @Autowired
    private InventoryCountService inventoryCountService;

    @GetMapping("/list")
    public List<InventoryCount> getInventoryCounts() {
        return inventoryCountService.getInventoryCounts();
    }

    @GetMapping("/get/{id}")
    public InventoryCountRequest getInventoryCountById(@PathVariable Integer id) {
        return inventoryCountService.getInventoryCountById(id);
    }

    @PostMapping("/saveOrUpdate")
    public InventoryCountRequest saveOrUpdateInventoryCount(@RequestBody InventoryCountRequest inventoryCountRequest) {
        return inventoryCountService.saveOrUpdateInventoryCount(inventoryCountRequest);
    }

    @PostMapping("/start/{id}")
    public InventoryCountRequest startInventoryCount(@PathVariable Integer id) {
        return inventoryCountService.startInventoryCount(id);
    }

    @PostMapping("/saveInventoryCountProduct")
    public String saveInventoryCountProduct(@RequestBody InventoryCountProduct inventoryCountProduct) {
        if(inventoryCountService.saveInventoryCountProduct(inventoryCountProduct) == true)
            return OperationConstants.SUCCESS;
        else
            return OperationConstants.FAIL;
    }

    @PostMapping("/abandon/{id}")
    public String abandonInventoryCount(@PathVariable Integer id) {
        if(inventoryCountService.abandonInventoryCount(id) == true)
            return OperationConstants.SUCCESS;
        else
            return OperationConstants.FAIL;
    }

    @PostMapping("/review/{id}")
    public String reviewInventoryCount(@PathVariable Integer id) {
        if(inventoryCountService.reviewInventoryCount(id) == true)
            return OperationConstants.SUCCESS;
        else
            return OperationConstants.FAIL;
    }
}
