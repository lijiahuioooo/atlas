package com.mfw.atlas.provider.server.http;

import com.mfw.atlas.client.model.ResponseResult;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.provider.model.dto.request.ServiceInstanceRequestDTO;
import com.mfw.atlas.provider.service.ProviderService;
import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发现中心服务管理
 *
 * @author huangrui
 */
@Api(tags = {"服务提供方（provider）管理"})
@RestController
@RequestMapping("/discovery/service")
@Slf4j
public class DiscoveryServiceController {

    @Autowired
    private ProviderService providerService;

    @ApiOperation(value = "获取服务实例列表")
    @GetMapping("instanceList")
    public ResponseResult<List<ProviderServiceDTO>> instanceList(@Valid ServiceInstanceRequestDTO request) {
        return ResponseResult.OK(providerService.getServiceInstances(request));
    }
}
