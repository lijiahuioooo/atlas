package com.mfw.atlas.provider.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mfw.atlas.provider.model.po.InstancePO;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 实例信息表 Mapper 接口
 * </p>
 *
 * @author jobob
 * @since 2020-10-21
 */
@Repository
public interface InstanceDao extends BaseMapper<InstancePO> {

}
