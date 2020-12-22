package ecommerce.app.backend.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BaseOrderProduct {

    @Id
    @Getter @Setter
    private String sku;

    @Getter @Setter
    private String productName;
}
