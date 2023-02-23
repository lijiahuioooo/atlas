package com.mfw.atlas.admin.convert;

import com.mfw.atlas.admin.constant.RegisterTypeEnum;
import com.mfw.atlas.admin.constant.ServiceTypeEnum;
import com.mfw.atlas.admin.model.bo.ConsumerServiceBO;
import com.mfw.atlas.admin.model.bo.InstanceBO;
import com.mfw.atlas.admin.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.admin.model.dto.ProviderServiceDTO;
import com.mfw.atlas.admin.model.po.ConsumerServicePO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

public class ConsumerServiceConvert {

    public static ConsumerServicePO toPO(ConsumerServiceDTO consumerServiceDTO) {
        ConsumerServicePO po = new ConsumerServicePO();
        BeanUtils.copyProperties(consumerServiceDTO, po);

        if(Objects.nonNull(consumerServiceDTO.getServiceType())){
            po.setServiceType(consumerServiceDTO.getServiceType().getCode());
        }

        if(Objects.nonNull(consumerServiceDTO.getRegisterType())){
            po.setRegisterType(consumerServiceDTO.getRegisterType().getCode());
        }

        if(Objects.isNull(consumerServiceDTO.getMetadata())){
            po.setMetadata("");
        }

        return po;
    }

    public static ConsumerServiceDTO toDTO(ConsumerServicePO consumerServicePO) {
        ConsumerServiceDTO consumerServiceDTO = new ConsumerServiceDTO();
        BeanUtils.copyProperties(consumerServicePO, consumerServiceDTO);

        if(Objects.nonNull(consumerServicePO.getRegisterType())){
            consumerServiceDTO.setRegisterType(RegisterTypeEnum.getByCode(consumerServicePO.getRegisterType()));
        }
        if(Objects.nonNull(consumerServicePO.getServiceType())){
            consumerServiceDTO.setServiceType(ServiceTypeEnum.getByCode(consumerServicePO.getServiceType()));
        }

        return consumerServiceDTO;
    }

    public static List<ConsumerServicePO> toPOList(List<ConsumerServiceDTO> consumerServiceDTOS) {
        List<ConsumerServicePO> consumerServicePOList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(consumerServiceDTOS)) {
            consumerServiceDTOS.forEach(consumerServiceDTO -> consumerServicePOList.add(toPO(consumerServiceDTO)));
        }
        return consumerServicePOList;
    }

    public static List<ConsumerServiceDTO> toDTOList(List<ConsumerServicePO> consumerServicePOS) {
        List<ConsumerServiceDTO> consumerServiceDTOS = new ArrayList<>();

        if (!CollectionUtils.isEmpty(consumerServicePOS)) {
            consumerServicePOS.forEach(consumerServicePO -> consumerServiceDTOS.add(toDTO(consumerServicePO)));
        }
        return consumerServiceDTOS;
    }

    public static ConsumerServiceBO toConsumerServiceBO(ConsumerServicePO consumerServicePO, InstanceBO instanceBO) {
        ConsumerServiceBO bo = ConsumerServiceBO.builder()
                .consumerServicePO(consumerServicePO)
                .build();
        BeanUtils.copyProperties(instanceBO,bo);
        return bo;
    }

    public static List<ConsumerServiceBO> toConsumerServiceBOList(List<ConsumerServicePO> consumerList, List<InstanceBO> instanceBOs, List<ProviderServiceDTO> providerServiceDTOS) {
        Map<String, String> appCodeInfoMapByServiceKey = new HashMap<>();
        for(ProviderServiceDTO providerServiceDTO : providerServiceDTOS) {
            appCodeInfoMapByServiceKey.put(providerServiceDTO.getServiceKey(), providerServiceDTO.getAppCode());
        }

        Map<String, InstanceBO> instanceBOMap = instanceBOs.stream()
                .collect(Collectors.toMap(InstanceBO::getInstanceId, Function.identity()));
        List<ConsumerServiceBO> boList = new ArrayList<>();
        consumerList.forEach((po) -> {
            if (po != null && instanceBOMap.get(po.getInstanceId()) != null) {
                ConsumerServiceBO bo = toConsumerServiceBO(po, instanceBOMap.get(po.getInstanceId()));
                bo.setConsumeServiceAppCode(appCodeInfoMapByServiceKey.get(bo.getConsumerServicePO().getServiceKey()));
                boList.add(bo);
            }
        });
        return boList;
    }
}
