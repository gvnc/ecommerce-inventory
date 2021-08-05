package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.BaseOrderProduct;
import org.springframework.data.repository.CrudRepository;

public interface BaseOrderProductRepository extends CrudRepository<BaseOrderProduct, String> {

}