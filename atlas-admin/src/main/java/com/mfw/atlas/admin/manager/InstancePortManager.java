package com.mfw.atlas.admin.manager;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mfw.atlas.admin.dao.InstancePortDao;
import com.mfw.atlas.admin.model.po.InstancePortPO;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


/**
 * 实例端口管理
 *
 * @author huangrui
 */
@Slf4j
@Component
public class InstancePortManager {

    @Autowired
    private InstancePortDao instancePortDao;


    /**
     * 根据instanceIds 查询所有
     *
     * @param instanceIds
     * @return
     */
    public List<InstancePortPO> selectListByInstanceId(List<String> instanceIds) {
        if (CollectionUtils.isEmpty(instanceIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<InstancePortPO> lambda = new QueryWrapper<InstancePortPO>().lambda();
        lambda.in(InstancePortPO::getInstanceId, instanceIds);

        List<InstancePortPO> instancePortPOList = instancePortDao.selectList(lambda);
        return instancePortPOList;
    }
}
