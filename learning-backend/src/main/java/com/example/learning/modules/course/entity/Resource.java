package com.example.learning.modules.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源实体，对应 resource 表。
 */
@Data
@TableName("resource")
public class Resource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long sectionId;

    private String type;

    private String url;

    private Long size;

    private LocalDateTime createdAt;
}