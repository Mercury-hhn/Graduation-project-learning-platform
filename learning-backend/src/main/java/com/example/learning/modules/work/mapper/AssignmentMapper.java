package com.example.learning.modules.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.work.entity.Assignment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业 Mapper。
 */
@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {
}