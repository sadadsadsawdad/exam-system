-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS exam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE exam;

-- 创建班级表
CREATE TABLE IF NOT EXISTS clazz (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '班级名称'
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS userlist (
    id INT PRIMARY KEY AUTO_INCREMENT,
    Username VARCHAR(50) NOT NULL COMMENT '用户名',
    Pwd VARCHAR(100) NOT NULL COMMENT '密码',
    role VARCHAR(20) DEFAULT 'student' COMMENT '角色: admin/teacher/student',
    class_id INT COMMENT '班级ID',
    avatar VARCHAR(255) COMMENT '头像URL',
    status INT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    UNIQUE KEY uk_username (Username),
    FOREIGN KEY (class_id) REFERENCES clazz(id) ON DELETE SET NULL
);

-- 插入默认班级
INSERT INTO clazz (name) VALUES ('默认班级');

-- 插入管理员账户（密码: admin123）
INSERT INTO userlist (Username, Pwd, role, status) VALUES ('admin', 'admin123', 'admin', 1);

-- 插入测试学生账户（密码: 123456）
INSERT INTO userlist (Username, Pwd, role, class_id, status) VALUES ('test', '123456', 'student', 1, 1);
