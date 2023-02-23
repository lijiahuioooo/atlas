package com.mfw.atlas.provider.test.manager;

import com.mfw.atlas.client.constants.RegisterTypeEnum;
import com.mfw.atlas.client.constants.ServiceTypeEnum;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.provider.manager.InstanceManager;
import com.mfw.atlas.provider.manager.ProviderServiceManager;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.model.dto.request.ServiceInstanceRequestDTO;
import com.mfw.atlas.provider.model.po.InstancePO;
import com.mfw.atlas.provider.model.po.InstancePortPO;
import com.mfw.atlas.provider.service.ProviderService;
import com.mfw.atlas.provider.test.AtlasTestApplication;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author KL
 * @Time 2020/11/19 11:16 上午
 */
@Slf4j
public class ProviderServiceManagerTest extends AtlasTestApplication {

    @Autowired
    private InstanceManager instanceManager;

    @Autowired
    private ProviderServiceManager providerServiceManager;

    @Autowired
    private ProviderService providerService;

    @Test
    public void insertOrUpdateBatch(){
        String instanceId = "nacos_test_02";
        InstancePO po = new InstancePO();
        po.setAppCode("111111");
        po.setCluster("111");
        po.setEnvType("dev");
        po.setEnvGroup("ray");
        po.setInstanceId(instanceId);
        po.setIp("127.0.0.1");
        po.setIsDelete(1);
        po.setState("开");
        po.setStatus(1);
        po.setReversion(0L);

        //端口
        List<InstancePortPO> instancePortPO = new ArrayList<>();
        InstancePortPO port = new InstancePortPO();
        port.setInstanceId(instanceId);
        port.setPort("8080");
        port.setProtocol("http");
        instancePortPO.add(port);

        InstanceBO bo = InstanceBO.builder()
                .instanceId(instanceId)
                .instancePO(po)
                .instancePortPOS(instancePortPO)
                .build();

        List<InstanceBO> instanceBOList = new ArrayList<>();
        instanceBOList.add(bo);

        instanceManager.insertOrUpdateBatch(instanceBOList);
    }

    @Test
    public void ProviderSaveBatch(){
        ProviderServiceDTO providerServiceDTO = new ProviderServiceDTO();

        providerServiceDTO.setInstanceId("321576-morder-msales-587bf994fb-lg6t4");
        providerServiceDTO.setServiceGroup("test11");
        providerServiceDTO.setServiceName("test");
        providerServiceDTO.setEnvType("dev");
        providerServiceDTO.setServiceType(ServiceTypeEnum.SPRING_CLOUD);
        providerServiceDTO.setServiceVersion("123");
        providerServiceDTO.setRegisterType(RegisterTypeEnum.NACOS);
        providerServiceDTO.setProtocol("udp");

        List<ProviderServiceDTO> consumerServiceDTOS = new ArrayList<>();
        consumerServiceDTOS.add(providerServiceDTO);

        providerServiceManager.saveBatch(consumerServiceDTOS);
    }

    @Test
    public void selectServiceInstances(){
        ServiceInstanceRequestDTO serviceInstanceRequestDTO = new ServiceInstanceRequestDTO();
        serviceInstanceRequestDTO.setEnvType("dev");
        serviceInstanceRequestDTO.setEnvGroup("ray");
        serviceInstanceRequestDTO.setServiceGroup("test");
        serviceInstanceRequestDTO.setServiceName("test");
        serviceInstanceRequestDTO.setServiceVersion("123");
        serviceInstanceRequestDTO.setServiceType(0);

        List<ProviderServiceDTO> serviceInstances = providerService.getServiceInstances(serviceInstanceRequestDTO);

        log.error(serviceInstances.toString());
    }

}
