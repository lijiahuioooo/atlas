package com.mfw.atlas.provider.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mfw.atlas.provider.model.po.ProviderServicePO;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 提供方信息表 Mapper 接口
 * </p>
 *
 * @author jobob
 * @since 2020-10-21
 */
@Repository
public interface ProviderServiceDao extends BaseMapper<ProviderServicePO> {

    int insert(List<ProviderServicePO> insertList);

    int updateBatch(List<ProviderServicePO> updateList);
}
