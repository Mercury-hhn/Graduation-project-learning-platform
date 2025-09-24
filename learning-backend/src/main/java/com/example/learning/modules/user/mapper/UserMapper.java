package com.example.learning.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserMapper：提供 user 表的 CRUD 操作。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}