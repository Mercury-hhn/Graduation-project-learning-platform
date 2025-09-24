package com.example.learning.modules.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节实体，对应 section 表。
 */
@Data
@TableName("section")
public class Section {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private String title;

    private Integer sort;

    private LocalDateTime createdAt;
}