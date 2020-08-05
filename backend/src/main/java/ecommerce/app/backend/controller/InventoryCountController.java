package ecommerce.app.backend.controller;


import ecommerce.app.backend.repository.InventoryCountRepository;
import ecommerce.app.backend.repository.model.InventoryCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200" })
@RequestMapping("/inventoryCount")
public class InventoryCountController {

    @Autowired
    private InventoryCountRepository inventoryCountRepository;

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
}
