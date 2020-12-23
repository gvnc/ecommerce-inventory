package ecommerce.app.backend.repository;

import ecommerce.app.backend.repository.model.BaseOrderItem;
import ecommerce.app.backend.repository.model.SalesHistory;
import ecommerce.app.backend.repository.model.SalesReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface BaseOrderItemRepository extends CrudRepository<BaseOrderItem, Integer> {

    @Query("select bop.productName as productName, sum(boi.quantity) as quantity, boi.sku as sku " +
            "from BaseOrderItem as boi join BaseOrderProduct as bop on boi.sku=bop.sku " +
            "where boi.quantity>0 and boi.insertDate >= ?1 and boi.insertDate <= ?2 group by boi.sku")
    List<SalesReport> quantitySumByDate(Date startDate, Date endDate);


    @Query("select bo.marketPlace as marketPlace, boi.quantity as quantity, boi.insertDate as insertDate " +
            "from BaseOrderItem as boi join BaseOrder as bo on boi.baseOrder = bo " +
            "where boi.sku = ?1 order by boi.insertDate desc")
    List<SalesHistory> getSalesByProductSku(String productSku);
}
