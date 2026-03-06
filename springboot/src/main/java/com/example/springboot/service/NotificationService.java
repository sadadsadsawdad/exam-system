package com.example.springboot.service;

import com.example.springboot.dao.NotificationMapper;
import com.example.springboot.dao.ExamUserMapper;
import com.example.springboot.dao.UserMapper;
import com.example.springboot.entity.Notification;
import com.example.springboot.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ExamUserMapper examUserMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 发送考试通知给相关用户
     * @param examId 考试ID
     * @param examTitle 考试标题
     * @param classId 班级ID（可为null）
     * @param startTime 开始时间
     * @param durationMinutes 考试时长（分钟）
     */
    public void sendExamNotification(Long examId, String examTitle, Long classId, String startTime, Integer durationMinutes) {
        List<Long> userIds = new ArrayList<>();
        
        // 1. 检查是否有指定用户
        int specifiedCount = examUserMapper.countByExamId(examId);
        if (specifiedCount > 0) {
            // 有指定用户，只通知指定的用户
            com.example.springboot.entity.ExamUser[] examUsers = examUserMapper.selectByExamId(examId);
            if (examUsers != null) {
                for (com.example.springboot.entity.ExamUser eu : examUsers) {
                    userIds.add(eu.getUserId());
                }
            }
        } else if (classId != null) {
            // 没有指定用户但有指定班级，通知该班级所有学生
            User[] users = userMapper.selectByClassId(classId);
            if (users != null) {
                for (User u : users) {
                    if (!"2".equals(u.getRole())) { // 排除管理员
                        userIds.add(u.getId());
                    }
                }
            }
        } else {
            // 没有指定用户也没有指定班级，通知所有学生
            User[] allUsers = userMapper.selectAll();
            if (allUsers != null) {
                for (User u : allUsers) {
                    if (!"2".equals(u.getRole())) { // 排除管理员
                        userIds.add(u.getId());
                    }
                }
            }
        }

        if (userIds.isEmpty()) {
            return;
        }

        // 构建通知内容
        String title = "新考试通知：" + examTitle;
        String durationStr = durationMinutes != null ? durationMinutes + "分钟" : "未设置";
        String content = String.format("您有一场新的考试【%s】，开始时间：%s，考试时长：%s，请及时参加！", 
                examTitle, startTime != null ? startTime : "未设置", durationStr);

        // 批量创建通知
        List<Notification> notifications = new ArrayList<>();
        for (Long userId : userIds) {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle(title);
            n.setContent(content);
            n.setType("EXAM");
            n.setRelatedId(examId);
            notifications.add(n);
        }

        if (!notifications.isEmpty()) {
            notificationMapper.batchInsert(notifications);
        }
    }

    /**
     * 获取用户的通知列表
     */
    public List<Notification> getUserNotifications(Long userId) {
        Notification[] arr = notificationMapper.selectByUserId(userId);
        if (arr == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(arr));
    }

    /**
     * 获取用户未读通知数量
     */
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 标记通知为已读
     */
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }

    /**
     * 标记用户所有通知为已读
     */
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsReadByUserId(userId);
    }

    /**
     * 删除通知
     */
    public void deleteNotification(Long id) {
        notificationMapper.delete(id);
    }
}
