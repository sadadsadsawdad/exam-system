package com.example.springboot.service;

import com.example.springboot.entity.Clazz;
import com.example.springboot.dao.ClazzMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ClazzService {
    @Autowired
    private ClazzMapper clazzMapper;

    /**
     * 获取所有班级
     */
    public List<Clazz> getAllClazzes() {
        Clazz[] clazzes = clazzMapper.selectAll();
        if (clazzes == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(clazzes));
    }

    /**
     * 根据ID获取班级
     */
    public Clazz getClazzById(Long id) {
        return clazzMapper.selectById(id);
    }

    /**
     * 根据名称获取班级
     */
    public Clazz getClazzByName(String name) {
        return clazzMapper.selectByName(name);
    }

    /**
     * 创建班级
     */
    public void createClazz(Clazz clazz) {
        clazzMapper.insert(clazz);
    }

    /**
     * 更新班级
     */
    public void updateClazz(Clazz clazz) {
        clazzMapper.update(clazz);
    }

    /**
     * 删除班级
     */
    public void deleteClazz(Long id) {
        clazzMapper.delete(id);
    }

    /**
     * 检查班级名称是否存在
     */
    public boolean existsByName(String name) {
        return clazzMapper.selectByName(name) != null;
    }

    /**
     * 获取班级总数
     */
    public int getClazzCount() {
        return clazzMapper.count();
    }
}
