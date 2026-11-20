package com.lingxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxi.entity.Artifact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产物 Mapper，基础数据操作由 MyBatis-Plus 提供。
 */
@Mapper
public interface ArtifactMapper extends BaseMapper<Artifact> {
}
