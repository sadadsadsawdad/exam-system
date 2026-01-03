package com.example.springboot.service;

import com.example.springboot.dao.MessageMapper;
import com.example.springboot.dao.NotificationMapper;
import com.example.springboot.dao.UserMapper;
import com.example.springboot.entity.Message;
import com.example.springboot.entity.Notification;
import com.example.springboot.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    public List<Message> getAllMessages() {
        return messageMapper.selectAll();
    }

    public List<Message> getMessagesByCondition(String scope, String status, String sender) {
        return messageMapper.selectByCondition(scope, status, sender);
    }

    public Message getMessageById(Long id) {
        return messageMapper.selectById(id);
    }

    public void createMessage(Message message) {
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (message.getCreateTime() == null) {
            message.setCreateTime(createTime);
        }
        if (message.getStatus() == null) {
            message.setStatus("draft");
        }
        if (message.getType() == null) {
            message.setType("manual");
        }
        messageMapper.insert(message);

        // 如果是已发布状态，同时创建用户通知
        if ("published".equals(message.getStatus())) {
            sendNotificationsToUsers(message, createTime);
        }
    }

    /**
     * 发送通知给用户
     */
    private void sendNotificationsToUsers(Message message, String createTime) {
        List<Long> userIds = new ArrayList<>();

        if ("all".equals(message.getScope())) {
            // 全部用户（排除管理员）
            User[] allUsers = userMapper.selectAll();
            if (allUsers != null) {
                for (User u : allUsers) {
                    if (!"2".equals(u.getRole())) {
                        userIds.add(u.getId());
                    }
                }
            }
        } else if ("class".equals(message.getScope()) && message.getClassId() != null) {
            // 指定班级的用户
            User[] users = userMapper.selectByClassId(message.getClassId());
            if (users != null) {
                for (User u : users) {
                    userIds.add(u.getId());
                }
            }
        }

        if (userIds.isEmpty()) {
            return;
        }

        // 批量创建通知
        List<Notification> notifications = new ArrayList<>();
        for (Long userId : userIds) {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle(message.getTitle());
            n.setContent(message.getContent());
            n.setType("MESSAGE");
            n.setRelatedId(message.getId());
            n.setCreateTime(createTime);
            n.setIsRead(0);
            notifications.add(n);
        }

        if (!notifications.isEmpty()) {
            notificationMapper.batchInsert(notifications);
        }
    }

    public void updateMessage(Message message) {
        messageMapper.update(message);
    }

    public void deleteMessage(Long id) {
        messageMapper.delete(id);
    }

    public void publishMessage(Long id) {
        messageMapper.updateStatus(id, "published");
        // 发布时也发送通知
        Message message = messageMapper.selectById(id);
        if (message != null) {
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            sendNotificationsToUsers(message, createTime);
        }
    }

    public void unpublishMessage(Long id) {
        messageMapper.updateStatus(id, "draft");
    }
}
