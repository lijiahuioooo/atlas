package com.mfw.atlas.provider.manager;

import com.mfw.atlas.provider.dao.InstanceLogDao;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.model.bo.InstanceLogInfoBO;
import com.mfw.atlas.provider.model.po.InstanceLogPO;
import com.mfw.atlas.provider.util.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 实例日历管理
 *
 * @author huangrui
 */
@Slf4j
@Component
public class InstanceLogManager {


    @Autowired
    private InstanceLogDao instanceLogDao;

    /**
     * 添加
     * @param instanceBO
     * @return
     */
    public Boolean insert(InstanceBO instanceBO) {
        if (instanceBO == null) {
            return false;
        }
        try {
            InstanceLogPO po = InstanceLogPO.builder()
                    .instanceId(instanceBO.getInstanceId())
                    //记录核心数据
                    .instanceInfo(GsonUtils.toJsonString(InstanceLogInfoBO.toInstanceLogInfoBO(instanceBO)))
                    .build();
            BeanUtils.copyProperties(instanceBO.getInstancePO(),po);
            instanceLogDao.insert(po);
            return po.getId() > 0;
        } catch (Exception e) {
            log.error("instanceLogDao  insert error,entity : {}" + GsonUtils.toJsonString(instanceBO), e);
        }
        return false;


    }

}
