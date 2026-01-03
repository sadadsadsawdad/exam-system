package com.example.springboot.controller;

import com.example.springboot.entity.Message;
import com.example.springboot.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取所有消息
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMessages(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sender) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Message> messages;
            if ((scope == null || scope.isEmpty()) && 
                (status == null || status.isEmpty()) && 
                (sender == null || sender.isEmpty())) {
                messages = messageService.getAllMessages();
            } else {
                messages = messageService.getMessagesByCondition(scope, status, sender);
            }
            result.put("code", 200);
            result.put("data", messages);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 根据ID获取消息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMessageById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Message message = messageService.getMessageById(id);
            if (message != null) {
                result.put("code", 200);
                result.put("data", message);
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 404);
                result.put("message", "消息不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 创建消息
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMessage(@RequestBody Message message) {
        Map<String, Object> result = new HashMap<>();
        try {
            messageService.createMessage(message);
            result.put("code", 200);
            result.put("message", "创建成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 更新消息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMessage(@PathVariable Long id, @RequestBody Message message) {
        Map<String, Object> result = new HashMap<>();
        try {
            message.setId(id);
            messageService.updateMessage(message);
            result.put("code", 200);
            result.put("message", "更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            messageService.deleteMessage(id);
            result.put("code", 200);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 发布消息
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishMessage(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            messageService.publishMessage(id);
            result.put("code", 200);
            result.put("message", "发布成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 取消发布
     */
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishMessage(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            messageService.unpublishMessage(id);
            result.put("code", 200);
            result.put("message", "取消发布成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
