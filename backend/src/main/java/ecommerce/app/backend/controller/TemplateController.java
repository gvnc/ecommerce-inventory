package ecommerce.app.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TemplateController {
    @RequestMapping(value={"/login", "/status", "/products", "/purchaseOrders", "/inventoryCounts", "/orderMonitoring"})
    public String HomePage() {
        return "index.html";
    }
}
