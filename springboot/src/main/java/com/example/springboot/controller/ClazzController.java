package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Clazz;
import com.example.springboot.entity.User;
import com.example.springboot.service.ClazzService;
import com.example.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理控制器
 */
@RestController
@RequestMapping("/api/clazz")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class ClazzController {

    @Autowired
    private ClazzService clazzService;

    @Autowired
    private UserService userService;

    /**
     * 获取所有班级
     */
    @GetMapping
    public Result<List<Clazz>> getAllClazzes() {
        List<Clazz> clazzes = clazzService.getAllClazzes();
        return Result.success(clazzes);
    }

    /**
     * 根据ID获取班级
     */
    @GetMapping("/{id}")
    public Result<Clazz> getClazzById(@PathVariable Long id) {
        Clazz clazz = clazzService.getClazzById(id);
        if (clazz == null) {
            return Result.error("班级不存在");
        }
        return Result.success(clazz);
    }

    /**
     * 创建班级
     */
    @PostMapping
    public Result<Clazz> createClazz(@RequestBody Clazz clazz) {
        if (clazz.getName() == null || clazz.getName().trim().isEmpty()) {
            return Result.error("班级名称不能为空");
        }

        if (clazzService.existsByName(clazz.getName())) {
            return Result.error("班级名称已存在");
        }

        clazzService.createClazz(clazz);
        return Result.success("班级创建成功", clazz);
    }

    /**
     * 更新班级
     */
    @PutMapping("/{id}")
    public Result<Clazz> updateClazz(@PathVariable Long id, @RequestBody Clazz clazz) {
        Clazz existingClazz = clazzService.getClazzById(id);
        if (existingClazz == null) {
            return Result.error("班级不存在");
        }

        // 检查名称是否被其他班级使用
        if (clazz.getName() != null && !clazz.getName().equals(existingClazz.getName())) {
            if (clazzService.existsByName(clazz.getName())) {
                return Result.error("班级名称已存在");
            }
        }

        clazz.setId(id);
        clazzService.updateClazz(clazz);
        return Result.success("班级更新成功", clazz);
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteClazz(@PathVariable Long id) {
        Clazz existingClazz = clazzService.getClazzById(id);
        if (existingClazz == null) {
            return Result.error("班级不存在");
        }

        clazzService.deleteClazz(id);
        return Result.success("班级删除成功", null);
    }

    /**
     * 获取班级学生列表
     */
    @GetMapping("/{id}/students")
    public Result<List<User>> getStudentsByClazzId(@PathVariable Long id) {
        Clazz existingClazz = clazzService.getClazzById(id);
        if (existingClazz == null) {
            return Result.error("班级不存在");
        }

        List<User> students = userService.getUsersByClassId(id);
        return Result.success(students);
    }

    /**
     * 添加学生到班级
     */
    @PostMapping("/{clazzId}/students/{userId}")
    public Result<Void> addStudentToClazz(@PathVariable Long clazzId, @PathVariable Long userId) {
        Clazz existingClazz = clazzService.getClazzById(clazzId);
        if (existingClazz == null) {
            return Result.error("班级不存在");
        }

        User user = userService.getUserById(userId.intValue());
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新用户的班级ID
        userService.updateUserClazz(userId, clazzId);
        return Result.success("添加成功", null);
    }

    /**
     * 从班级移除学生
     */
    @DeleteMapping("/{clazzId}/students/{userId}")
    public Result<Void> removeStudentFromClazz(@PathVariable Long clazzId, @PathVariable Long userId) {
        Clazz existingClazz = clazzService.getClazzById(clazzId);
        if (existingClazz == null) {
            return Result.error("班级不存在");
        }

        User user = userService.getUserById(userId.intValue());
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 清除用户的班级ID
        userService.updateUserClazz(userId, null);
        return Result.success("移除成功", null);
    }
}
