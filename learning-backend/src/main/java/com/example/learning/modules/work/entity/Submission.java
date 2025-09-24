package com.example.learning.modules.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交实体，对应 submission 表。
 */
@Data
@TableName("submission")
public class Submission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assignmentId;

    private Long userId;

    private String content;

    private String attachment;

    private Integer score;

    private String aiComment;

    private LocalDateTime createdAt;

    private LocalDateTime gradedAt;
}