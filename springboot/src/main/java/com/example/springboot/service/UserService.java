package com.example.springboot.service;

import com.example.springboot.entity.User;
import com.example.springboot.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        User[] users = userMapper.selectAll();
        if (users == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(users));
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 根据邮箱获取用户
     */
    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    /**
     * 根据手机号获取用户
     */
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    /**
     * 创建用户
     */
    public void createUser(User user) {
        // 设置默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 设置创建时间
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now().format(DATE_FORMATTER));
        }
        userMapper.insert(user);
    }

    /**
     * 更新用户信息
     */
    public void updateUser(User user) {
        userMapper.update(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Integer id) {
        userMapper.delete(id);
    }

    /**
     * 批量删除用户
     */
    public void batchDeleteUsers(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            userMapper.batchDelete(ids);
        }
    }

    /**
     * 更新密码
     */
    public void updatePassword(Integer id, String newPassword) {
        userMapper.updatePassword(id, newPassword);
    }

    /**
     * 更新用户状态（启用/禁用）
     */
    public void updateStatus(Integer id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    /**
     * 批量更新用户状态
     */
    public void batchUpdateStatus(List<Integer> ids, Integer status) {
        if (ids != null && !ids.isEmpty()) {
            userMapper.batchUpdateStatus(ids, status);
        }
    }

    /**
     * 更新最后登录信息
     */
    public void updateLoginInfo(Integer id, String ip) {
        String loginTime = LocalDateTime.now().format(DATE_FORMATTER);
        userMapper.updateLoginInfo(id, loginTime, ip);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userMapper.selectByUsername(username) != null;
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userMapper.selectByEmail(email) != null;
    }

    /**
     * 检查手机号是否存在
     */
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return userMapper.selectByPhone(phone) != null;
    }

    /**
     * 搜索用户（支持用户名、角色、状态过滤）
     */
    public List<User> searchUsers(String username, String role, Integer status) {
        User[] users = userMapper.searchUsers(username, role, status);
        if (users == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(users));
    }

    /**
     * 搜索用户（兼容旧接口）
     */
    public List<User> searchUsers(String username, String role) {
        return searchUsers(username, role, null);
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }
        if (user.getPassword() == null) {
            return null;
        }
        if (!user.getPassword().equals(password)) {
            return null;
        }
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            return null; // 账户已禁用
        }
        return user;
    }

    /**
     * 用户登录（带IP记录）
     */
    public User login(String username, String password, String ip) {
        User user = login(username, password);
        if (user != null) {
            // 更新最后登录信息
            updateLoginInfo(user.getId().intValue(), ip);
        }
        return user;
    }

    /**
     * 获取用户总数
     */
    public int getUserCount() {
        return userMapper.count();
    }

    /**
     * 根据角色获取用户数量
     */
    public int getUserCountByRole(String role) {
        return userMapper.countByRole(role);
    }

    /**
     * 根据状态获取用户数量
     */
    public int getUserCountByStatus(Integer status) {
        return userMapper.countByStatus(status);
    }

    /**
     * 根据班级ID获取用户列表
     */
    public List<User> getUsersByClassId(Long classId) {
        User[] users = userMapper.selectByClassId(classId);
        if (users == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(users));
    }

    /**
     * 更新用户的班级
     */
    public void updateUserClazz(Long userId, Long clazzId) {
        userMapper.updateClazzId(userId, clazzId);
    }
}