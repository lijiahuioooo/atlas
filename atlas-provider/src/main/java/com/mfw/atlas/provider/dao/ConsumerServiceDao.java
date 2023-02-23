package com.mfw.atlas.provider.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mfw.atlas.provider.model.po.ConsumerServicePO;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 消费方信息表 Mapper 接口
 * </p>
 *
 * @author jobob
 * @since 2020-10-21
 */
@Repository
public interface ConsumerServiceDao extends BaseMapper<ConsumerServicePO> {

    int insert(List<ConsumerServicePO> insertList);

    int updateBatch(List<ConsumerServicePO> updateList);
}
