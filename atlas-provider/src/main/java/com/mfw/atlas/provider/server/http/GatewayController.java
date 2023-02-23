package com.mfw.atlas.provider.server.http;

import com.mfw.atlas.client.model.ResponseResult;
import com.mfw.atlas.provider.manager.GatewayServiceManager;
import com.mfw.atlas.provider.manager.InstanceManager;
import com.mfw.atlas.provider.model.dto.response.GatewayInstanceDTO;
import com.mfw.atlas.provider.model.dto.response.InstanceKubsDTO;
import com.mfw.atlas.provider.util.GsonUtils;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关服务
 * @author huangrui
 */
@Api(tags = {"网关调用相关"})
@RestController
@RequestMapping("/gateway")
@Slf4j
public class GatewayController {

    @Autowired
    private GatewayServiceManager gatewayServiceManager;

    @Autowired
    private InstanceManager instanceManager;

    @ApiOperation(value = "网关实例上线")
    @PostMapping("online")
    public ResponseResult<Boolean> online(@RequestBody @Validated GatewayInstanceDTO gatewayInstanceDTO){
        return ResponseResult.OK(gatewayServiceManager.insertOrUpdate(gatewayInstanceDTO));
    }

    @ApiOperation(value = "网关实例下线")
    @PostMapping("offline")
    public ResponseResult<Boolean> offline(@RequestBody GatewayInstanceDTO gatewayInstanceDTO){
        return ResponseResult.OK(gatewayServiceManager.offline(gatewayInstanceDTO));
    }

    @ApiOperation(value = "网关查询实例信息列表")
    @GetMapping("queryInstances")
    public ResponseResult<List<InstanceKubsDTO>> getGatewayInstances(@RequestParam(name="enabled") Integer enabled,@RequestParam(name="envTypes",defaultValue="") String envTypes){
        return ResponseResult.OK(instanceManager.queryAll(enabled,envTypes));
    }

    @ApiOperation(value = "网关查询实例信息")
    @GetMapping("getInstance")
    public ResponseResult<InstanceKubsDTO> getInstance(@RequestParam(name = "instanceId") String instanceId) {
        InstanceKubsDTO instanceKubsDTO = instanceManager.gateWayFindInstance(instanceId);
        return ResponseResult.OK(instanceKubsDTO);
    }
}
