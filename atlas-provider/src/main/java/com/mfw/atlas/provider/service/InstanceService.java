package com.mfw.atlas.provider.service;

import com.mfw.atlas.provider.constant.InstanceStatusEnum;
import com.mfw.atlas.provider.convert.InstanceConvert;
import com.mfw.atlas.provider.manager.InstanceManager;
import com.mfw.atlas.provider.model.dto.request.QueryInstanceRequestDTO;
import com.mfw.atlas.provider.model.dto.request.ServiceInstanceRequestDTO;
import com.mfw.atlas.provider.model.dto.response.InstanceOnlineDTO;
import com.mfw.atlas.provider.model.po.InstancePO;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author KL
 * @Time 2020/11/19 9:52 上午
 */
@Slf4j
@Service
public class InstanceService {

    @Autowired
    private InstanceManager instanceManager;

    public List<InstanceOnlineDTO> getInstanceByAppcode(String appcode) {

        List<InstancePO> instanceByAppcode = instanceManager
                .getInstanceByAppcode(appcode, InstanceStatusEnum.ENABLE.getCode());

        ServiceInstanceRequestDTO serviceInstanceRequestDTO = new ServiceInstanceRequestDTO();

        serviceInstanceRequestDTO.setEnvType("product");

        List<InstancePO> defaultVersion = new ArrayList<>();
        if(!CollectionUtils.isEmpty(instanceByAppcode)){
            defaultVersion = instanceManager
                    .getDefaultVersion(instanceByAppcode, serviceInstanceRequestDTO);
        }

        return InstanceConvert.toInstanceOnlineDTOS(defaultVersion);
    }

    public List<InstanceOnlineDTO> queryInstance(QueryInstanceRequestDTO queryInfo) {
        List<InstancePO> instancePOList = instanceManager.queryInstance(queryInfo);
        return InstanceConvert.toInstanceOnlineDTOS(instancePOList);
    }

}
