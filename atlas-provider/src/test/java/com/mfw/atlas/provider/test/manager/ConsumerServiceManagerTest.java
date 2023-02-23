package com.mfw.atlas.provider.test.manager;

import com.mfw.atlas.client.constants.RegisterTypeEnum;
import com.mfw.atlas.client.constants.ServiceTypeEnum;
import com.mfw.atlas.client.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.provider.manager.ConsumerServiceManager;
import com.mfw.atlas.provider.model.bo.ConsumerServiceBO;
import com.mfw.atlas.provider.test.AtlasTestApplication;
import com.mfw.atlas.provider.util.GsonUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ConsumerServiceManagerTest extends AtlasTestApplication {

    @Autowired
    private ConsumerServiceManager consumerServiceManager;

    @Test
    public void getByProviderInstanceId() {
        String instanceId = "nacos_test_02";
        List<ConsumerServiceBO> result = consumerServiceManager.getByProviderInstanceId(instanceId);
        Assert.assertNotNull(result);
        log.info("test getByProviderInstanceId result :{}", GsonUtils.toJsonString(result));
    }

    @Test
    public void getByInstanceId() {
        String instanceId = "nacos_test_01";
        List<ConsumerServiceBO> result = consumerServiceManager.getByInstanceId(instanceId);
        Assert.assertNotNull(result);
        log.info("test getByInstanceId result :{}", GsonUtils.toJsonString(result));
    }

    @Test
    public void ConsumerSaveBatch(){
        ConsumerServiceDTO consumerServiceDTO = new ConsumerServiceDTO();


        consumerServiceDTO.setInstanceId("test3");
        consumerServiceDTO.setServiceGroup("test");
        consumerServiceDTO.setServiceName("test");
        consumerServiceDTO.setServiceType(ServiceTypeEnum.SPRING_CLOUD);
        consumerServiceDTO.setServiceVersion("123");
        consumerServiceDTO.setRegisterType(RegisterTypeEnum.NACOS);
        consumerServiceDTO.setProtocol("udp");

        List<ConsumerServiceDTO> consumerServiceDTOS = new ArrayList<>();
        consumerServiceDTOS.add(consumerServiceDTO);

        consumerServiceManager.saveBatch(consumerServiceDTOS);
    }

    @Test
    public void deleteConsumer(){
        consumerServiceManager.deleteById(47L);
    }
}
