package com.example.springboot.dao;

import com.example.springboot.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamMapper {

    Exam[] selectAll();

    Exam selectById(Long id);

    void insert(Exam exam);

    void update(Exam exam);

    void delete(Long id);
}
