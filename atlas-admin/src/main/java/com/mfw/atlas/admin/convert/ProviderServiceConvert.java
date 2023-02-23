package com.mfw.atlas.admin.convert;

import com.mfw.atlas.admin.constant.RegisterTypeEnum;
import com.mfw.atlas.admin.constant.ServiceTypeEnum;
import com.mfw.atlas.admin.model.dto.ProviderServiceDTO;
import com.mfw.atlas.admin.model.dto.ServiceInstanceRequestDTO;
import com.mfw.atlas.admin.model.po.ProviderServicePO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

public class ProviderServiceConvert {

    public static ProviderServicePO toPO(ProviderServiceDTO providerServiceDTO) {
        ProviderServicePO po = new ProviderServicePO();
        if (providerServiceDTO != null) {
            BeanUtils.copyProperties(providerServiceDTO, po);
            po.setServiceType(providerServiceDTO.getServiceType().getCode());
            po.setRegisterType(providerServiceDTO.getRegisterType().getCode());
            if(Objects.isNull(providerServiceDTO.getMetadata())){
                po.setMetadata("");
            }
        }
        return po;
    }

    public static ProviderServiceDTO toDTO(ProviderServicePO providerServicePO) {
        ProviderServiceDTO providerServiceDTO = new ProviderServiceDTO();
        if (providerServicePO != null) {
            BeanUtils.copyProperties(providerServicePO, providerServiceDTO);
        }
        return providerServiceDTO;
    }

    public static List<ProviderServicePO> toPOList(List<ProviderServiceDTO> providerServiceDTOS) {
        List<ProviderServicePO> providerServicePOList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(providerServiceDTOS)) {
            providerServiceDTOS.forEach(providerServicePO -> providerServicePOList.add(toPO(providerServicePO)));
        }
        return providerServicePOList;
    }

    public static List<ProviderServiceDTO> toDTOList(List<ProviderServicePO> providerServicePOS) {
        List<ProviderServiceDTO> providerServicePOList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(providerServicePOS)) {
            providerServicePOS.forEach(providerServicePO -> providerServicePOList.add(toDTO(providerServicePO)));
        }
        return providerServicePOList;
    }

    public static ProviderServiceDTO serviceDTOToProviderDTO(ServiceInstanceRequestDTO serviceInstanceRequestDTO) {
        ProviderServiceDTO providerServiceDTO = new ProviderServiceDTO();
        if (serviceInstanceRequestDTO != null) {
            providerServiceDTO.setServiceType(ServiceTypeEnum.getByCode(serviceInstanceRequestDTO.getServiceType()));
            BeanUtils.copyProperties(serviceInstanceRequestDTO, providerServiceDTO);
        }
        return providerServiceDTO;
    }
}
