package com.example.springboot.dao;

import com.example.springboot.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper {
    
    // 插入通知
    void insert(Notification notification);
    
    // 批量插入通知
    void batchInsert(@Param("list") java.util.List<Notification> list);
    
    // 查询用户的通知列表
    Notification[] selectByUserId(Long userId);
    
    // 查询用户未读通知数量
    int countUnreadByUserId(Long userId);
    
    // 标记通知为已读
    void markAsRead(Long id);
    
    // 标记用户所有通知为已读
    void markAllAsReadByUserId(Long userId);
    
    // 删除通知
    void delete(Long id);
}
