package com.mfw.atlas.provider.convert;

import com.mfw.atlas.client.utils.StringUtils;
import com.mfw.atlas.provider.model.dto.request.DiscoveryExtensionServiceDTO;
import com.mfw.atlas.provider.model.po.DiscoveryExtensionServicePO;
import java.nio.charset.Charset;
import org.springframework.beans.BeanUtils;
import org.springframework.web.util.UriUtils;

public class DiscoveryExtensionServiceConvert {

    public static DiscoveryExtensionServicePO toPO(DiscoveryExtensionServiceDTO dto) {
        DiscoveryExtensionServicePO po = new DiscoveryExtensionServicePO();
        BeanUtils.copyProperties(dto, po);

        if (StringUtils.isNotEmpty(dto.getMetadata())) {
            po.setMetadata(UriUtils.decode(dto.getMetadata(),Charset.defaultCharset()));
        }
        if (StringUtils.isNotEmpty(dto.getVersions())) {
            po.setVersions(UriUtils.decode(dto.getVersions(),Charset.defaultCharset()));
        }
        return po;
    }

    public static DiscoveryExtensionServiceDTO toDTO(DiscoveryExtensionServicePO po) {
        DiscoveryExtensionServiceDTO dto = new DiscoveryExtensionServiceDTO();
        BeanUtils.copyProperties(po, dto);
        return dto;
    }
}
