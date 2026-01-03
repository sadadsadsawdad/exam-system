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
    
    void update(ExamResult result);
    
    List<ExamResult> selectForGrading(@Param("status") String status, 
                                      @Param("examId") Long examId, 
                                      @Param("keyword") String keyword);
}
