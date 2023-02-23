package com.mfw.atlas.provider.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mfw.atlas.provider.model.po.ConsumerServicePO;
import com.mfw.atlas.provider.model.po.DiscoveryExtensionServicePO;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 服务发现扩展服务
 *
 * @author jobob
 * @since 2020-10-21
 */
@Repository
public interface DiscoveryExtensionServiceDao extends BaseMapper<DiscoveryExtensionServicePO> {
}
