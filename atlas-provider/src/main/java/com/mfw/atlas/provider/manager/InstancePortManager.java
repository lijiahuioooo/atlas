package com.mfw.atlas.provider.manager;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mfw.atlas.provider.dao.InstancePortDao;
import com.mfw.atlas.provider.model.po.InstancePortPO;
import java.util.ArrayList;
import java.util.List;

import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


/**
 * 实例端口管理
 *
 * @author huangrui
 */
@Slf4j
@Component
public class InstancePortManager {

    @Autowired
    private InstancePortDao instancePortDao;


    /**
     * 根据instanceIds 查询所有
     *
     * @param instanceIds
     * @return
     */
    public List<InstancePortPO> selectListByInstanceIds(List<String> instanceIds, String serviceType) {
        if (CollectionUtils.isEmpty(instanceIds) || StringUtils.isEmpty(serviceType)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<InstancePortPO> lambda = new QueryWrapper<InstancePortPO>().lambda();
        lambda.in(InstancePortPO::getInstanceId, instanceIds);
        lambda.eq(InstancePortPO::getProtocol,serviceType);

        List<InstancePortPO> instancePortPOList = instancePortDao.selectList(lambda);
        return instancePortPOList;
    }

    public  boolean removeOfflineInstancePort(List<String> instanceIds) {
        if (!CollectionUtils.isEmpty(instanceIds)) {
            LambdaQueryWrapper<InstancePortPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(InstancePortPO::getInstanceId, instanceIds);
            int num = instancePortDao.delete(wrapper);
            XxlJobLogger.log("remove port num:" + num);
        }

        return true;
    }
}
