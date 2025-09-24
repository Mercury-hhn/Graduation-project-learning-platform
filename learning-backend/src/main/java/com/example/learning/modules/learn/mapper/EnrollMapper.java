package com.example.learning.modules.learn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.learn.entity.Enroll;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选课 Mapper。
 */
@Mapper
public interface EnrollMapper extends BaseMapper<Enroll> {
}