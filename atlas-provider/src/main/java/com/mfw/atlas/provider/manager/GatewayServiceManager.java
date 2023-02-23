package com.mfw.atlas.provider.manager;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mfw.atlas.client.constants.GlobalStatusConstants;
import com.mfw.atlas.provider.constant.InstanceEnableEnum;
import com.mfw.atlas.provider.convert.GatewayServiceConvert;
import com.mfw.atlas.provider.dao.GatewayInstanceDao;
import com.mfw.atlas.provider.model.dto.response.GatewayInstanceDTO;
import com.mfw.atlas.provider.model.po.GatewayInstancePO;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 网关实例管理
 * @author huangrui
 */
@Slf4j
@Component
public class GatewayServiceManager {

    @Autowired
    private GatewayInstanceDao gatewayInstanceDao;

    /**
     * 添加
     * @param gatewayInstanceDTO
     * @return
     */
    public Boolean insertOrUpdate(GatewayInstanceDTO gatewayInstanceDTO) {

        LambdaQueryWrapper<GatewayInstancePO> gatewayPOWrapper = new QueryWrapper<GatewayInstancePO>().lambda();
        gatewayPOWrapper.eq(GatewayInstancePO::getInstanceId, gatewayInstanceDTO.getInstanceId());
        gatewayPOWrapper.eq(GatewayInstancePO::getEnvType, gatewayInstanceDTO.getEnvType());
        gatewayPOWrapper.eq(GatewayInstancePO::getIp, gatewayInstanceDTO.getIp());
        gatewayPOWrapper.eq(GatewayInstancePO::getPort, gatewayInstanceDTO.getPort());
        GatewayInstancePO selectGateway = gatewayInstanceDao.selectOne(gatewayPOWrapper);

        if(Objects.nonNull(selectGateway)){
            GatewayInstancePO gatewayInstancePO = GatewayServiceConvert.toPO(gatewayInstanceDTO);
            gatewayInstanceDao.update(gatewayInstancePO,gatewayPOWrapper);
        }else{
            GatewayInstancePO po = GatewayServiceConvert.toPO(gatewayInstanceDTO);
            gatewayInstanceDao.insert(po);
        }

        return true;
    }

    /**
     * 下线
     * @param gatewayInstanceDTO
     * @return
     */
    public boolean offline(GatewayInstanceDTO gatewayInstanceDTO) {
        UpdateWrapper<GatewayInstancePO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(GatewayInstancePO::getInstanceId, gatewayInstanceDTO.getInstanceId());
        gatewayInstanceDTO.setEnabled(InstanceEnableEnum.DISABLE.getCode());
        int update = gatewayInstanceDao.update(GatewayServiceConvert.toPO(gatewayInstanceDTO), updateWrapper);
        return update == 1;
    }

    /**
     * 获取所有网关可用实例信息
     * @return
     */
    public List<GatewayInstanceDTO> getGatewayInstances() {
        List<GatewayInstancePO> poList = gatewayInstanceDao
                .selectList(new QueryWrapper<GatewayInstancePO>().lambda()
                        .eq(GatewayInstancePO::getIsDelete, GlobalStatusConstants.IS_DELETE_DISABLE)
                        .eq(GatewayInstancePO::getEnabled, InstanceEnableEnum.ENABLE.getCode()));
        return GatewayServiceConvert.toDTOList(poList);
    }
}
