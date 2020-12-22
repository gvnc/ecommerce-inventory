package ecommerce.app.backend.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class BaseOrder {

    @Id
    @Getter @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    private String marketPlace;

    @Getter @Setter
    private String orderId;

    @Getter @Setter
    private Float totalPrice;

    @Getter @Setter
    private String status;

    @Getter @Setter
    private Date insertDate;
}
