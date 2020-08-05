package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.InventoryCount;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InventoryCountRepository extends CrudRepository<InventoryCount, Integer> {

    List<InventoryCount> findAllByOrderByIdDesc();
}
