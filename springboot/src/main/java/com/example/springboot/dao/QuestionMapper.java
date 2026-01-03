package com.example.springboot.dao;

import com.example.springboot.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionMapper {
    Question[] selectAll();

    void insert(Question question);

    void update(Question question);

    void delete(Long id);

    /**
     * 按题型随机获取指定数量的题目
     */
    Question[] selectRandomByType(@Param("type") Integer type, @Param("count") Integer count);

    /**
     * 按题型统计题目数量
     */
    int countByType(@Param("type") Integer type);

    /**
     * 根据ID查询题目
     */
    Question selectById(@Param("id") Long id);
}
