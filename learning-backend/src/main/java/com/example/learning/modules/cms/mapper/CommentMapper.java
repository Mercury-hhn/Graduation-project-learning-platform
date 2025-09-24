package com.example.learning.modules.cms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.cms.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留言 Mapper。
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}