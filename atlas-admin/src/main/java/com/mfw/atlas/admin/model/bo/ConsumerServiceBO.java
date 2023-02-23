package com.mfw.atlas.admin.model.bo;

import com.mfw.atlas.admin.model.po.ConsumerServicePO;
import com.mfw.atlas.admin.model.po.InstancePO;
import com.mfw.atlas.admin.model.po.InstancePortPO;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * @author KL
 * @Time 2020/11/2 4:28 下午
 */
@Data
@Builder
public class ConsumerServiceBO {

    private InstancePO instancePO;

    private List<InstancePortPO> instancePortPO;

    private ConsumerServicePO consumerServicePO;

    private String consumeServiceAppCode;

}
