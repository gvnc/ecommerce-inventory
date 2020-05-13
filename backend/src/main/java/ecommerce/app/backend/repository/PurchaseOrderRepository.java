package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.PurchaseOrder;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PurchaseOrderRepository extends CrudRepository<PurchaseOrder, Integer> {

    List<PurchaseOrder> findAllByOrderByIdDesc();
}
