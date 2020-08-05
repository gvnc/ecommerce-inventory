package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.InventoryCountProduct;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InventoryCountProductRepository extends CrudRepository<InventoryCountProduct, Integer> {

    List<InventoryCountProduct> findAllByInventoryCount_Id(Integer inventoryCountId);

    void deleteByInventoryCount_Id(Integer inventoryCountId);
}