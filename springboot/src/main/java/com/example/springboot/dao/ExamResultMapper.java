package com.example.springboot.dao;

import com.example.springboot.entity.ExamResult;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExamResultMapper {

    ExamResult[] selectByUserId(Long userId);

    ExamResult[] selectAll();

    ExamResult selectById(Long id);

    void insert(ExamResult result);

    void deleteByExamId(Long examId);
    
    void deleteByUserId(Long userId);
    
    ExamResult[] selectByExamId(Long examId);
    
    void update(ExamResult result);
    
    List<ExamResult> selectForGrading(@Param("status") String status, 
                                      @Param("examId") Long examId, 
                                      @Param("keyword") String keyword);
    
    /**
     * 修复缺考记录的批改状态
     */
    int fixAbsentGradingStatus();
}
