package com.example.learning.modules.learn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learning.modules.learn.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏 Mapper。
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}