package ecommerce.app.backend.controller;

import ecommerce.app.backend.repository.model.SalesHistory;
import ecommerce.app.backend.repository.model.SalesReport;
import ecommerce.app.backend.service.ReportService;
import ecommerce.app.backend.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = { "http://95.111.250.92:3000", "http://localhost:3000", "http://localhost:4200" })
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    private final Long oneDay = 24 * 60 * 60 * 1000L;

    @GetMapping("/sales")
    public List<SalesReport> getSales(String startDate, String endDate) {
        Date start = Utils.getDateFromString(startDate);
        Date end = Utils.getDateFromString(endDate);
        Date newEndDate = new Date(end.getTime() + oneDay);
        return reportService.getSalesReport(start, newEndDate);
    }

    @GetMapping("/sales/{productSku}")
    public List<SalesHistory> getSalesByProductSku(@PathVariable String productSku) {
        return reportService.getSalesByProductSku(productSku);
    }
}
