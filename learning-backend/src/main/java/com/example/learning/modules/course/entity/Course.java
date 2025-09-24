package com.example.learning.modules.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体，对应 course 表。
 */
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String cover;

    private String tags;

    private Long teacherId;

    private Integer status;

    private Integer viewCount;

    private Integer enrollCount;

    private LocalDateTime createdAt;
}