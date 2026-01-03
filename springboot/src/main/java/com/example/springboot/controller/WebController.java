package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class WebController {
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "Hello World";
    }

    @GetMapping("/index")
    public String home() {
        return "Welcome to the Home Page";
    }

    @GetMapping("/users")
    public List<com.example.springboot.entity.User> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody com.example.springboot.entity.User loginRequest, 
                                   HttpServletRequest request) {
        if (loginRequest == null) {
            return new ResponseEntity<>(Result.error("请求参数错误"), HttpStatus.BAD_REQUEST);
        }
        
        // 先检查用户是否存在
        User existingUser = userService.getUserByUsername(loginRequest.getUsername());
        if (existingUser == null) {
            return new ResponseEntity<>(Result.error("用户名或密码错误"), HttpStatus.UNAUTHORIZED);
        }
        
        // 检查用户状态
        if (existingUser.getStatus() != null && existingUser.getStatus() == 0) {
            return new ResponseEntity<>(Result.error("账户已被禁用，请联系管理员"), HttpStatus.FORBIDDEN);
        }
        
        // 获取客户端IP
        String clientIp = getClientIp(request);
        
        // 登录验证（带IP记录）
        com.example.springboot.entity.User user = userService.login(
            loginRequest.getUsername(), 
            loginRequest.getPassword(), 
            clientIp
        );
        
        if (user == null) {
            return new ResponseEntity<>(Result.error("用户名或密码错误"), HttpStatus.UNAUTHORIZED);
        }
        
        user.setPassword(null);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}