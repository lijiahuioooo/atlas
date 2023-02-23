package com.mfw.atlas.provider.server.http;

import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.client.model.ResponseResult;
import com.mfw.atlas.client.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.provider.exceptions.BusinessException;
import com.mfw.atlas.provider.manager.ConsumerServiceManager;
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
 * 消费方元数据管理
 * @author huangrui
 */
@Api(tags = {"消费方（consumer）元数据管理"})
@RestController
@RequestMapping("/consumer/metadata")
@Slf4j
public class ConsumerMetadataController {

    @Autowired
    private ConsumerServiceManager consumerServiceManager;

    @ApiOperation(value = "消费方元数据上报")
    @PostMapping("report")
    public ResponseResult<Boolean> report(@RequestBody List<ConsumerServiceDTO> consumerServiceDTOS){
        try{
            return ResponseResult.OK(consumerServiceManager.saveBatch(consumerServiceDTOS));
        } catch (Exception e){
            log.info("reportconsumererror:{}",consumerServiceDTOS);
            throw new BusinessException(GlobalCodeEnum.GL_SUBSCRIBE_FAIL_9002.getCode(),e.getMessage());
        }
    }

}
