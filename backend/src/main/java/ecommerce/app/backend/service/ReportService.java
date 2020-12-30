package ecommerce.app.backend.service;

import ecommerce.app.backend.repository.BaseOrderItemRepository;
import ecommerce.app.backend.repository.model.InventoryChange;
import ecommerce.app.backend.repository.model.SalesReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private BaseOrderItemRepository baseOrderItemRepository;

    public List<SalesReport> getSalesReport(Date startDate, Date endDate){
        return baseOrderItemRepository.quantitySumByDate(startDate, endDate);
    }

    public List<InventoryChange> getnventoryChangesByProductSku(String productSku){
        return baseOrderItemRepository.getnventoryChangesByProductSku(productSku);
    }

}
