CREATE DATABASE IF NOT EXISTS web1_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE web1_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    user_name VARCHAR(30) NOT NULL UNIQUE COMMENT '用户账号',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nick_name VARCHAR(30) COMMENT '用户昵称',
    status CHAR(1) DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
    email VARCHAR(50) COMMENT '用户邮箱',
    phonenumber VARCHAR(11) COMMENT '手机号码',
    sex CHAR(1) COMMENT '用户性别（0男 1女 2未知）',
    avatar VARCHAR(100) COMMENT '头像地址',
    user_type VARCHAR(2) DEFAULT '01' COMMENT '用户类型（00系统用户01注册用户）',
    del_flag INT DEFAULT 0 COMMENT '删除标志（0代表存在 2代表删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 插入测试用户数据（密码为123456的BCrypt加密）
INSERT INTO sys_user (user_name, password, nick_name, email, status) VALUES 
('admin', '$2a$10$7JB720yubVSOfvVMe6/b/.b4OXhF2.zWuWkOjX6wjGtQGVDwhn9AO', '管理员', 'admin@example.com', '0'),
('test', '$2a$10$7JB720yubVSOfvVMe6/b/.b4OXhF2.zWuWkOjX6wjGtQGVDwhn9AO', '测试用户', 'test@example.com', '0');