package com.example.springboot.dao;

import com.example.springboot.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 根据ID查询用户
     */
    User selectById(Integer id);

    /**
     * 查询所有用户
     */
    User[] selectAll();

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    User selectByPhone(String phone);

    /**
     * 搜索用户（支持用户名、角色、状态过滤）
     */
    User[] searchUsers(@Param("username") String username, 
                       @Param("role") String role,
                       @Param("status") Integer status);

    /**
     * 删除用户
     */
    void delete(Integer id);

    /**
     * 批量删除用户
     */
    void batchDelete(@Param("ids") List<Integer> ids);

    /**
     * 插入用户
     */
    void insert(User user);

    /**
     * 更新用户信息
     */
    void update(User user);

    /**
     * 更新密码
     */
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    /**
     * 更新用户状态（启用/禁用）
     */
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 批量更新用户状态
     */
    void batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

    /**
     * 更新最后登录信息
     */
    void updateLoginInfo(@Param("id") Integer id, 
                         @Param("lastLoginTime") String lastLoginTime, 
                         @Param("lastLoginIp") String lastLoginIp);

    /**
     * 统计用户总数
     */
    int count();

    /**
     * 根据角色统计用户数量
     */
    int countByRole(String role);

    /**
     * 根据状态统计用户数量
     */
    int countByStatus(Integer status);

    /**
     * 根据班级ID查询用户
     */
    User[] selectByClassId(Long classId);

    /**
     * 更新用户的班级ID
     */
    void updateClazzId(@Param("userId") Long userId, @Param("clazzId") Long clazzId);
}