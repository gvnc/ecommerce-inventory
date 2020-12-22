package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.BaseOrder;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BaseOrderRepository extends CrudRepository<BaseOrder, Integer> {

    List<BaseOrder> findAllByOrderByIdDesc();
}
