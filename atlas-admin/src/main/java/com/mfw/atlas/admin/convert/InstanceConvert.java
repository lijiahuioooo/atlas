package com.mfw.atlas.admin.convert;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mfw.atlas.admin.model.bo.InstanceBO;
import com.mfw.atlas.admin.model.po.InstancePO;
import com.mfw.atlas.admin.model.po.InstancePortPO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author
 */
public class InstanceConvert {

    public static List<InstanceBO> toInstanceBOList(List<InstancePO> instancePOS,
            List<InstancePortPO> instancePortPOS) {
        Map<String, List<InstancePortPO>> instancePortMap = instancePortPOS.stream()
                .collect(Collectors.groupingBy(InstancePortPO::getInstanceId));
        List<InstanceBO> boList = new ArrayList<>();
        instancePOS.forEach((po) -> {
            InstanceBO bo = toInstanceBO(po, instancePortMap.get(po.getInstanceId()));
            BeanUtils.copyProperties(po, bo);
            boList.add(bo);
        });
        return boList;
    }

    public static InstanceBO toInstanceBO(InstancePO instancePO, List<InstancePortPO> instancePortPOS) {
        return InstanceBO.builder()
                .instanceId(instancePO.getInstanceId())
                .instancePO(instancePO)
                .instancePortPOS(instancePortPOS)
                .build();
    }
}
