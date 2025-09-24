package com.example.learning.modules.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.work.entity.Submission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业提交 Mapper。
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {
}