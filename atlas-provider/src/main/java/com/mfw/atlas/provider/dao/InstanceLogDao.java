package com.mfw.atlas.provider.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mfw.atlas.provider.model.po.InstanceLogPO;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 实例kbs信息推送记录表 Mapper 接口
 * </p>
 *
 * @author jobob
 * @since 2020-10-21
 */
@Repository
public interface InstanceLogDao extends BaseMapper<InstanceLogPO> {

}
