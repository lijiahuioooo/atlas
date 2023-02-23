package com.mfw.atlas.provider.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.JsonObject;
import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.client.utils.StringUtils;
import com.mfw.atlas.provider.convert.DiscoveryExtensionServiceConvert;
import com.mfw.atlas.provider.dao.DiscoveryExtensionServiceDao;
import com.mfw.atlas.provider.exceptions.BusinessException;
import com.mfw.atlas.provider.model.dto.request.DiscoveryExtensionServiceDTO;
import com.mfw.atlas.provider.model.po.DiscoveryExtensionServicePO;
import com.mfw.atlas.provider.service.InstanceChangeService;
import com.mfw.atlas.provider.util.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;


/**
 * 服务发现扩展服务管理
 *
 * @author huangrui
 */
@Slf4j
@Component
public class DiscoveryExtensionServiceManager {

    @Autowired
    private DiscoveryExtensionServiceDao discoveryExtensionServiceDao;

    @Autowired
    private InstanceChangeService instanceChangeService;

    public boolean setMetadata(DiscoveryExtensionServiceDTO discoveryExtensionServiceDTO) {
        basicSet(discoveryExtensionServiceDTO);
        return true;
    }

    public boolean removeMetadata(DiscoveryExtensionServiceDTO discoveryExtensionServiceDTO) {
        QueryWrapper<DiscoveryExtensionServicePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DiscoveryExtensionServicePO::getAppcode, discoveryExtensionServiceDTO.getAppcode());
        queryWrapper.lambda().eq(DiscoveryExtensionServicePO::getEnvType, discoveryExtensionServiceDTO.getEnvType());

        DiscoveryExtensionServicePO discoveryExtensionServicePO = discoveryExtensionServiceDao.selectOne(queryWrapper);
        boolean bUpdate = false;
        if (Objects.nonNull(discoveryExtensionServicePO)) {

            JsonObject removeMetaData = GsonUtils.toJsonObject(UriUtils.decode(discoveryExtensionServiceDTO.getMetadata(),Charset.defaultCharset()));
            JsonObject newMetaData = GsonUtils.toJsonObject(discoveryExtensionServicePO.getMetadata());
            if(Objects.nonNull(removeMetaData) && Objects.nonNull(newMetaData)){
                for (String keyStr : removeMetaData.keySet()) {
                    if(Objects.nonNull(newMetaData.get(keyStr)) &&
                            newMetaData.get(keyStr).equals(removeMetaData.get(keyStr))){
                        newMetaData.addProperty(keyStr, "");
                        bUpdate = true;
                    }
                }
            }
            if(bUpdate) {
                discoveryExtensionServiceDTO.setId(discoveryExtensionServicePO.getId());
                discoveryExtensionServiceDTO.setMetadata(GsonUtils.toJsonString(newMetaData));
                discoveryExtensionServiceDao
                        .updateById(DiscoveryExtensionServiceConvert.toPO(discoveryExtensionServiceDTO));
            }
        }
        return true;
    }

    public boolean setVersions(DiscoveryExtensionServiceDTO discoveryExtensionServiceDTO) {
        basicSet(discoveryExtensionServiceDTO);
        //有更新version则通知sdk 优先通过指定的version查找实例
        if(Objects.nonNull(discoveryExtensionServiceDTO.getVersions())) {
            String version = "";
            String versionStr = UriUtils.decode(discoveryExtensionServiceDTO.getVersions(),Charset.defaultCharset());
            if(StringUtils.isNotEmpty(versionStr)) {
                JsonObject versions = GsonUtils.toJsonObject(versionStr);
                for(String v: versions.keySet()) {
                    if(Objects.nonNull(v)) {
                        Double weight = versions.get(v).getAsDouble();
                        if(weight > 0) {
                            version = v;
                            break;
                        }
                    }
                }
            }

            instanceChangeService.publishEventForSDK(discoveryExtensionServiceDTO.getAppcode(),
                    discoveryExtensionServiceDTO.getEnvType(), version);
        }

        return true;
    }

    private void basicSet(DiscoveryExtensionServiceDTO discoveryExtensionServiceDTO) {
        QueryWrapper<DiscoveryExtensionServicePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DiscoveryExtensionServicePO::getAppcode, discoveryExtensionServiceDTO.getAppcode());
        queryWrapper.lambda().eq(DiscoveryExtensionServicePO::getEnvType, discoveryExtensionServiceDTO.getEnvType());

        DiscoveryExtensionServicePO discoveryExtensionServicePO = discoveryExtensionServiceDao.selectOne(queryWrapper);

        if (Objects.nonNull(discoveryExtensionServicePO)) {
            discoveryExtensionServiceDTO.setId(discoveryExtensionServicePO.getId());
            discoveryExtensionServiceDao
                    .updateById(DiscoveryExtensionServiceConvert.toPO(discoveryExtensionServiceDTO));
        } else {
            try {
                discoveryExtensionServiceDao
                        .insert(DiscoveryExtensionServiceConvert.toPO(discoveryExtensionServiceDTO));
            } catch (Exception e) {
                throw new BusinessException(GlobalCodeEnum.GL_INSERT_FAIL_9011.getCode(),
                        GlobalCodeEnum.GL_INSERT_FAIL_9011.getDesc());
            }
        }
    }

    public List<DiscoveryExtensionServicePO> selectExtensionByAppcode(List<String> appcodes) {
        QueryWrapper<DiscoveryExtensionServicePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(DiscoveryExtensionServicePO::getAppcode, appcodes);

        return discoveryExtensionServiceDao.selectList(queryWrapper);
    }

    public DiscoveryExtensionServicePO getExtensionByAppcodeAndEnvtype(String appcode, String envtype) {
        QueryWrapper<DiscoveryExtensionServicePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DiscoveryExtensionServicePO::getAppcode, appcode);
        queryWrapper.lambda().eq(DiscoveryExtensionServicePO::getEnvType, envtype);

        DiscoveryExtensionServicePO discoveryExtensionServicePO = discoveryExtensionServiceDao.selectOne(queryWrapper);

        return discoveryExtensionServicePO;
    }

}
