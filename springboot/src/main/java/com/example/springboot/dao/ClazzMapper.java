package com.example.springboot.dao;

import com.example.springboot.entity.Clazz;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClazzMapper {
    /**
     * 根据ID查询班级
     */
    Clazz selectById(Long id);

    /**
     * 查询所有班级
     */
    Clazz[] selectAll();

    /**
     * 根据名称查询班级
     */
    Clazz selectByName(String name);

    /**
     * 插入班级
     */
    void insert(Clazz clazz);

    /**
     * 更新班级
     */
    void update(Clazz clazz);

    /**
     * 删除班级
     */
    void delete(Long id);

    /**
     * 统计班级总数
     */
    int count();
}
