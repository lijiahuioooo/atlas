package com.mfw.atlas.provider.server.http;

import com.mfw.atlas.client.model.ResponseResult;
import com.mfw.atlas.provider.manager.DiscoveryExtensionServiceManager;
import com.mfw.atlas.provider.model.dto.request.DiscoveryExtensionServiceDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"扩展信息管理"})
@RestController
@RequestMapping("/discovery/extension/service")
public class DiscoveryExtensionServiceController {

    @Autowired
    private DiscoveryExtensionServiceManager discoveryExtensionServiceManager;

    /**
     * 配置发布
     * @param discoveryExtensionServiceDTO
     * @return
     */
    @ApiOperation(value = "设置扩展信息")
    @PostMapping("setMetadata")
    public ResponseResult<Boolean> setMetadata(@RequestBody DiscoveryExtensionServiceDTO discoveryExtensionServiceDTO) {
        return ResponseResult.OK(discoveryExtensionServiceManager.setMetadata(discoveryExtensionServiceDTO));
    }

    /**
     * 配置发布
     * @param discoveryExtensionServiceDTO
     * @return
     */
    @ApiOperation(value = "删除扩展信息")
    @PostMapping("removeMetadata")
    public ResponseResult<Boolean> removeMetadata(@RequestBody DiscoveryExtensionServiceDTO discoveryExtensionServiceDTO) {
        return ResponseResult.OK(discoveryExtensionServiceManager.removeMetadata(discoveryExtensionServiceDTO));
    }

    /**
     * 设置版本信息
     * @param discoveryExtensionServiceDTO
     * @return
     */
    @ApiOperation(value = "设置扩展信息")
    @PostMapping("setVersions")
    public ResponseResult<Boolean> setVersions(@RequestBody DiscoveryExtensionServiceDTO discoveryExtensionServiceDTO) {
        return ResponseResult.OK(discoveryExtensionServiceManager.setVersions(discoveryExtensionServiceDTO));
    }
}
