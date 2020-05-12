package ecommece.app.backend.repository;

import ecommece.app.backend.repository.model.PurchaseOrder;
import org.springframework.data.repository.CrudRepository;

public interface PurchaseOrderRepository extends CrudRepository<PurchaseOrder, Integer> {


}
