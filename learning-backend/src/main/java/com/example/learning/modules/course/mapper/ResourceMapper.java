package com.example.learning.modules.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.course.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资源 Mapper。
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
}