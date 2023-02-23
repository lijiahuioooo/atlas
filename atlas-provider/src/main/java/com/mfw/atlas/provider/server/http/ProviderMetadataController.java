package com.mfw.atlas.provider.server.http;

import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.client.model.ResponseResult;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.provider.exceptions.BusinessException;
import com.mfw.atlas.provider.manager.ProviderServiceManager;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发现中心元数据管理
 * @author huangrui
 */
@Api(tags = {"服务提供方（provider）元数据管理"})
@RestController
@RequestMapping("/provider/metadata")
@Slf4j
public class ProviderMetadataController {

    @Autowired
    private ProviderServiceManager providerServiceManager;

    /**
     * 元数据上报
     * @param providerServiceDTOS
     * @return
     */
    @ApiOperation(value = "服务提供方（provider）元数据上报")
    @PostMapping("report")
    public ResponseResult<Boolean> report(@RequestBody List<ProviderServiceDTO> providerServiceDTOS){
        try{
            return ResponseResult.OK(providerServiceManager.saveBatch(providerServiceDTOS));
        } catch (Exception e){
            throw new BusinessException(GlobalCodeEnum.GL_REGISTER_FAIL_9001.getCode(),e.getMessage());
        }

    }
}
