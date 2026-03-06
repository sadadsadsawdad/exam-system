package com.example.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        
        // 测试数据库连接
        try (Connection conn = dataSource.getConnection()) {
            status.put("database", "UP");
            status.put("database_url", conn.getMetaData().getURL());
        } catch (Exception e) {
            status.put("database", "DOWN");
            status.put("database_error", e.getMessage());
        }
        
        return status;
    }

    @GetMapping("/api/health")
    public Map<String, Object> apiHealth() {
        return health();
    }
}