package com.mfw.atlas.provider.server.http;

import com.mfw.atlas.client.model.ResponseResult;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.model.dto.request.QueryInstanceRequestDTO;
import com.mfw.atlas.provider.model.dto.response.InstanceOnlineDTO;
import com.mfw.atlas.provider.service.InstanceChangeService;
import com.mfw.atlas.provider.service.InstanceService;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author KL
 * @Time 2020/11/18 5:20 下午
 */
@Api(tags = {"实例接口"})
@RestController
@RequestMapping("/instance")
public class InstanceServerController {

    @Autowired
    private InstanceChangeService instanceChangeService;

    @Autowired
    private InstanceService instanceService;

    @ApiOperation(value = "同步实例")
    @PostMapping("synInstance")
    public ResponseResult<Boolean> synInstance(@RequestBody InstanceBO instanceBO){
        instanceChangeService.synInstance(instanceBO);
        return ResponseResult.OK(true);
    }

    @ApiOperation(value = "获取实例信息")
    @GetMapping("getInstance")
    public ResponseResult<List<InstanceOnlineDTO>> getInstance(String appcode){
        return ResponseResult.OK(instanceService.getInstanceByAppcode(appcode));
    }

    @ApiOperation(value = "查询实例信息")
    @GetMapping("queryInstance")
    public ResponseResult<List<InstanceOnlineDTO>> queryInstance(@Valid QueryInstanceRequestDTO request){
        return ResponseResult.OK(instanceService.queryInstance(request));
    }
}
