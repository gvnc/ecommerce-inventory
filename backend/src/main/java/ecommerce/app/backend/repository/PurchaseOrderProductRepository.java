package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.PurchaseOrderProduct;
import org.springframework.data.repository.CrudRepository;

public interface PurchaseOrderProductRepository extends CrudRepository<PurchaseOrderProduct, Integer> {

    Iterable<PurchaseOrderProduct> findAllByPurchaseOrder_Id(Integer purchaseOrderId);

}