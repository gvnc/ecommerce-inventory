package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.BaseOrder;
import org.springframework.data.repository.CrudRepository;

public interface BaseOrderRepository extends CrudRepository<BaseOrder, Integer> {

    BaseOrder findOneByOrderId(String orderId);
}
