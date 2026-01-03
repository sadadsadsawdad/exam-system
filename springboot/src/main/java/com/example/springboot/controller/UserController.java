package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户
     */
    @GetMapping
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // 隐藏密码
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 创建用户（注册）
     */
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        // 参数校验
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 检查用户名是否已存在
        if (userService.existsByUsername(user.getUsername())) {
            return Result.error("用户名已存在");
        }

        // 设置默认角色为普通用户
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("1"); // 1表示普通用户
        }

        userService.createUser(user);
        user.setPassword(null);
        return Result.success("用户创建成功", user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        // 检查用户名是否被其他用户使用
        if (user.getUsername() != null && !user.getUsername().equals(existingUser.getUsername())) {
            if (userService.existsByUsername(user.getUsername())) {
                return Result.error("用户名已存在");
            }
        }

        // 检查邮箱是否被其他用户使用
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userService.existsByEmail(user.getEmail())) {
                return Result.error("邮箱已被使用");
            }
        }

        user.setId(Long.valueOf(id));
        // 如果没有传密码，保留原密码
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        }

        userService.updateUser(user);
        user.setPassword(null);
        return Result.success("用户更新成功", user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Integer id) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        userService.deleteUser(id);
        return Result.success("用户删除成功", null);
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Integer id, @RequestBody PasswordChangeRequest request) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证旧密码
        if (!user.getPassword().equals(request.getOldPassword())) {
            return Result.error("原密码错误");
        }

        // 验证新密码
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        userService.updatePassword(id, request.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    /**
     * 重置密码（管理员功能）
     */
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 重置为默认密码 123456
        userService.updatePassword(id, "123456");
        return Result.success("密码已重置为 123456", null);
    }

    /**
     * 根据用户名搜索用户
     */
    @GetMapping("/search")
    public Result<List<User>> searchUsers(@RequestParam(required = false) String username,
                                          @RequestParam(required = false) String role,
                                          @RequestParam(required = false) Integer status) {
        List<User> users = userService.searchUsers(username, role, status);
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id, @RequestBody StatusUpdateRequest request) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            return Result.error("状态值无效，应为0（禁用）或1（启用）");
        }

        userService.updateStatus(id, request.getStatus());
        return Result.success(request.getStatus() == 1 ? "用户已启用" : "用户已禁用", null);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteUsers(@RequestBody BatchIdsRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的用户");
        }

        userService.batchDeleteUsers(request.getIds());
        return Result.success("批量删除成功", null);
    }

    /**
     * 批量启用/禁用用户
     */
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(@RequestBody BatchStatusRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要操作的用户");
        }

        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            return Result.error("状态值无效，应为0（禁用）或1（启用）");
        }

        userService.batchUpdateStatus(request.getIds(), request.getStatus());
        return Result.success(request.getStatus() == 1 ? "批量启用成功" : "批量禁用成功", null);
    }

    /**
     * 获取当前用户信息（个人资料）
     */
    @GetMapping("/profile/{id}")
    public Result<User> getProfile(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 更新个人资料（仅头像）
     */
    @PutMapping("/profile/{id}")
    public Result<User> updateProfile(@PathVariable Integer id, @RequestBody User user) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        // 只允许更新头像
        User updateUser = new User();
        updateUser.setId(Long.valueOf(id));
        if (user.getAvatar() != null) {
            updateUser.setAvatar(user.getAvatar());
        }

        userService.updateUser(updateUser);
        
        User updatedUser = userService.getUserById(id);
        updatedUser.setPassword(null);
        return Result.success("头像更新成功", updatedUser);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public Result<UserStatistics> getStatistics() {
        UserStatistics stats = new UserStatistics();
        stats.setTotalCount(userService.getUserCount());
        stats.setStudentCount(userService.getUserCountByRole("1"));
        stats.setAdminCount(userService.getUserCountByRole("2"));
        stats.setActiveCount(userService.getUserCountByStatus(1));
        stats.setDisabledCount(userService.getUserCountByStatus(0));
        return Result.success(stats);
    }

    /**
     * 密码修改请求类
     */
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    /**
     * 状态更新请求类
     */
    public static class StatusUpdateRequest {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    /**
     * 批量ID请求类
     */
    public static class BatchIdsRequest {
        private List<Integer> ids;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }
    }

    /**
     * 批量状态更新请求类
     */
    public static class BatchStatusRequest {
        private List<Integer> ids;
        private Integer status;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    /**
     * 用户统计信息类
     */
    public static class UserStatistics {
        private int totalCount;
        private int studentCount;
        private int adminCount;
        private int activeCount;
        private int disabledCount;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getStudentCount() {
            return studentCount;
        }

        public void setStudentCount(int studentCount) {
            this.studentCount = studentCount;
        }

        public int getAdminCount() {
            return adminCount;
        }

        public void setAdminCount(int adminCount) {
            this.adminCount = adminCount;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public void setActiveCount(int activeCount) {
            this.activeCount = activeCount;
        }

        public int getDisabledCount() {
            return disabledCount;
        }

        public void setDisabledCount(int disabledCount) {
            this.disabledCount = disabledCount;
        }
    }
}
