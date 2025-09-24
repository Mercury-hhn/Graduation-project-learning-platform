package com.example.learning.modules.cms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.cms.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告 Mapper。
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}