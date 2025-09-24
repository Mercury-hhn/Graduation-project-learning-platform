package com.example.learning.modules.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.course.entity.Section;
import org.apache.ibatis.annotations.Mapper;

/**
 * 章节 Mapper。
 */
@Mapper
public interface SectionMapper extends BaseMapper<Section> {
}