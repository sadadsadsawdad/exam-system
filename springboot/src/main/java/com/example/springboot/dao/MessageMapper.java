package com.example.springboot.dao;

import com.example.springboot.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    
    // 插入消息
    void insert(Message message);
    
    // 查询所有消息
    List<Message> selectAll();
    
    // 根据条件查询消息
    List<Message> selectByCondition(@Param("scope") String scope, 
                                     @Param("status") String status, 
                                     @Param("sender") String sender);
    
    // 根据ID查询
    Message selectById(Long id);
    
    // 更新消息
    void update(Message message);
    
    // 删除消息
    void delete(Long id);
    
    // 更新发布状态
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}
