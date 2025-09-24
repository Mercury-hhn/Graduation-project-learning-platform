package com.example.learning.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体：对应 user 表，支持邮箱/手机号登录。
 */
@Data
@TableName("user")
public class User {

    /**
     * 主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邮箱地址，唯一。
     */
    private String email;

    /**
     * 手机号，唯一。
     */
    private String phone;

    /**
     * 用户名，可选字段。
     */
    private String username;

    /**
     * 密码哈希，OTP 登录可为空。
     */
    private String passwordHash;

    /**
     * 角色，取值 STUDENT/TEACHER/ADMIN。
     */
    private String role;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 头像地址。
     */
    private String avatar;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}