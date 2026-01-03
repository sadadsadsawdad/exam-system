package com.example.springboot.dao;

import com.example.springboot.entity.ExamUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamUserMapper {
    
    // 批量插入考试用户关联
    void batchInsert(@Param("examId") Long examId, @Param("userIds") List<Long> userIds);
    
    // 删除考试的所有用户关联
    void deleteByExamId(Long examId);
    
    // 查询考试的指定用户列表
    ExamUser[] selectByExamId(Long examId);
    
    // 检查用户是否被指定参加考试
    int checkUserInExam(@Param("examId") Long examId, @Param("userId") Long userId);
    
    // 查询考试是否有指定用户（用于判断是全员还是指定）
    int countByExamId(Long examId);
}
