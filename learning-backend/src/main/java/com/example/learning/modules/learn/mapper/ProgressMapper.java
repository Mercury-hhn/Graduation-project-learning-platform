package com.example.learning.modules.learn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.learn.entity.Progress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习进度 Mapper。
 */
@Mapper
public interface ProgressMapper extends BaseMapper<Progress> {
}