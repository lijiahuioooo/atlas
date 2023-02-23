package com.mfw.atlas.provider.convert;

import com.mfw.atlas.provider.model.dto.response.GatewayInstanceDTO;
import com.mfw.atlas.provider.model.po.GatewayInstancePO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

public class GatewayServiceConvert {

    public static GatewayInstancePO toPO(GatewayInstanceDTO gatewayInstanceDTO) {
        GatewayInstancePO po = new GatewayInstancePO();
        if (null != gatewayInstanceDTO) {
            BeanUtils.copyProperties(gatewayInstanceDTO, po);
        }
        return po;
    }

    public static GatewayInstanceDTO toDTO(GatewayInstancePO gatewayInstancePO) {
        GatewayInstanceDTO providerServiceDTO = new GatewayInstanceDTO();
        if (null != gatewayInstancePO) {
            BeanUtils.copyProperties(gatewayInstancePO, providerServiceDTO);
        }
        return providerServiceDTO;
    }

    public static List<GatewayInstancePO> toPOList(List<GatewayInstanceDTO> gatewayInstanceDTOS) {
        List<GatewayInstancePO> gatewayInstancePOList = new ArrayList<>();

        if (null != gatewayInstanceDTOS && !CollectionUtils.isEmpty(gatewayInstanceDTOS)) {
            gatewayInstanceDTOS.forEach(gatewayInstancePO -> gatewayInstancePOList.add(toPO(gatewayInstancePO)));
        }
        return gatewayInstancePOList;
    }

    public static List<GatewayInstanceDTO> toDTOList(List<GatewayInstancePO> gatewayInstancePOS) {
        List<GatewayInstanceDTO> gatewayInstanceDTOList = new ArrayList<>();

        if (null != gatewayInstancePOS && !CollectionUtils.isEmpty(gatewayInstancePOS)) {
            gatewayInstancePOS.forEach(gatewayInstancePO -> gatewayInstanceDTOList.add(toDTO(gatewayInstancePO)));
        }
        return gatewayInstanceDTOList;
    }

}
