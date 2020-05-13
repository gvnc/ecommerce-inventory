package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.PurchaseOrder;
import org.springframework.data.repository.CrudRepository;

public interface PurchaseOrderRepository extends CrudRepository<PurchaseOrder, Integer> {


}
