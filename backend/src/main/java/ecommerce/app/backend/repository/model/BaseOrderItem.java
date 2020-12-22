package ecommerce.app.backend.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
public class BaseOrderItem {

    @Id
    @Getter @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @OneToOne
    private BaseOrder baseOrder;

    @Getter @Setter
    private String sku;

    @Getter @Setter
    private Integer quantity;

    @Getter @Setter
    private Date insertDate;
}
