package com.bookstore.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping({"/", "/api"})
    public ResponseEntity<Map<String, Object>> getApiOverview() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "E-Commerce Bookstore REST API");
        response.put("status", "UP");
        response.put("version", "1.0.0");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("books", "/api/v1/books");
        endpoints.put("customers", "/api/v1/customers");
        endpoints.put("carts", "/api/v1/carts/customer/{customerId}");
        endpoints.put("orders", "/api/v1/orders/customer/{customerId}");
        endpoints.put("checkout", "/api/v1/orders/checkout");

        response.put("available_endpoints", endpoints);
        return ResponseEntity.ok(response);
    }
}
