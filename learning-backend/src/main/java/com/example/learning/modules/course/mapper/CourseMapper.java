package com.example.learning.modules.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程 Mapper，提供基础 CRUD。
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}