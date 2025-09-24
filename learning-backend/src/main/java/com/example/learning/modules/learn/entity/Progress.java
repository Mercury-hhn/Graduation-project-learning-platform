package com.example.learning.modules.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习进度实体。
 */
@Data
@TableName("progress")
public class Progress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    private Long sectionId;

    private Integer progress;

    private LocalDateTime lastLearnAt;
}