package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.PurchaseOrderAttachment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PurchaseOrderAttachmentRepository extends CrudRepository<PurchaseOrderAttachment, Integer> {
    PurchaseOrderAttachment findByPurchaseOrder_Id(Integer purchaseOrderId);

    void deleteByPurchaseOrder_Id(Integer purchaseOrderId);
}