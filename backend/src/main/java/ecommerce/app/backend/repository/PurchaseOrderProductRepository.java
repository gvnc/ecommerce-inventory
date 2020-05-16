package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.PurchaseOrderProduct;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PurchaseOrderProductRepository extends CrudRepository<PurchaseOrderProduct, Integer> {

    List<PurchaseOrderProduct> findAllByPurchaseOrder_Id(Integer purchaseOrderId);

    void deleteByPurchaseOrder_Id(Integer purchaseOrderId);

}