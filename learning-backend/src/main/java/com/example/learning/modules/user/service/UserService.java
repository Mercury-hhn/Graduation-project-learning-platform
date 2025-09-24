package com.example.learning.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learning.modules.user.entity.User;
import com.example.learning.modules.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务：支持根据邮箱/手机号查询或创建用户，默认角色 STUDENT。
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据邮箱查找用户。
     */
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)));
    }

    /**
     * 根据手机号查找用户。
     */
    public Optional<User> findByPhone(String phone) {
        if (phone == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)));
    }

    /**
     * 根据 ID 查询用户。
     */
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectById(id));
    }

    /**
     * 根据邮箱或手机号获取用户，没有则自动创建。
     */
    @Transactional
    public User getOrCreate(String channel, String receiver) {
        Optional<User> existing;
        if ("email".equalsIgnoreCase(channel)) {
            existing = findByEmail(receiver);
        } else {
            existing = findByPhone(receiver);
        }
        if (existing.isPresent()) {
            return existing.get();
        }
        User user = new User();
        if ("email".equalsIgnoreCase(channel)) {
            user.setEmail(receiver);
            user.setNickname(receiver);
        } else {
            user.setPhone(receiver);
            user.setNickname(receiver);
        }
        user.setRole("STUDENT");
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }
}