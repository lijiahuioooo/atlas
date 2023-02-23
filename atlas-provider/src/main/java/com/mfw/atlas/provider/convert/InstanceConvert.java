package com.mfw.atlas.provider.convert;

import com.mfw.atlas.client.utils.StringUtils;
import com.mfw.atlas.provider.constant.InstanceEnableEnum;
import com.mfw.atlas.provider.grpc.InstanceOuterClass;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.GetAllInstancesRequest;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.Instance;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.PortInfo;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.model.bo.InstanceLbBO;
import com.mfw.atlas.provider.model.dto.request.GetAllInstancesRequestDTO;
import com.mfw.atlas.provider.model.dto.response.InstanceKubsDTO;
import com.mfw.atlas.provider.model.dto.response.InstanceKubsPortDTO;
import com.mfw.atlas.provider.model.dto.response.InstanceOnlineDTO;
import com.mfw.atlas.provider.model.po.InstancePO;
import com.mfw.atlas.provider.model.po.InstancePortPO;
import com.mfw.atlas.provider.util.GsonUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author
 */
public class InstanceConvert {


    public static List<InstanceBO> toBOList(List<Instance> instanceList) {
        List<InstanceBO> result = new ArrayList<>();

        if (!CollectionUtils.isEmpty(instanceList)) {
            instanceList.forEach(instance -> {
                if (instance != null) {
                    result.add(toBO(instance));
                }
            });
        }
        return result;
    }

    public static InstanceBO toBO(Instance instance) {
        if (instance == null) {
            return InstanceBO.builder().build();
        }
        InstancePO po = InstancePO.builder()
                .enabled(instance.getEnabled() ? InstanceEnableEnum.ENABLE.getCode() : InstanceEnableEnum.DISABLE.getCode())
                .cpu(Float.valueOf(instance.getCpu()).intValue())
                .image(GsonUtils.toJsonString(instance.getImageMap()))
                .label(GsonUtils.toJsonString(instance.getLabelMap()))
                .appName(instance.getLabelOrDefault("env:san",instance.getAppCode()))
                .build();
        BeanUtils.copyProperties(instance, po);

        List<InstancePortPO> instancePortPO = new ArrayList<>();
        for (InstanceOuterClass.PortInfo portInfo : instance.getPortsList()) {
            if (portInfo == null || portInfo.getProtocol() == null) {
                continue;
            }
            InstancePortPO portPO = InstancePortPO.builder()
                    .port(String.valueOf(portInfo.getPort()))
                    .protocol(portInfo.getProtocol())
                    .name(portInfo.getName())
                    .instanceId(instance.getInstanceId())
                    .build();
            instancePortPO.add(portPO);
        }
        return InstanceBO.builder()
                .instanceId(instance.getInstanceId())
                .instancePO(po)
                .instancePortPOS(instancePortPO)
                .build();
    }

    public static Instance toGrpcInstance(InstanceBO bo) {
        Map<String, String> image = new HashMap<>(8);
        if (StringUtils.isNotEmpty(bo.getInstancePO().getImage())) {
            image = GsonUtils.fromJson(bo.getInstancePO().getImage(), Map.class);
        }
        Map<String, String> label = new HashMap<>(8);
        if (StringUtils.isNotEmpty(bo.getInstancePO().getLabel())) {
            label = GsonUtils.fromJson(bo.getInstancePO().getLabel(), Map.class);
        }
        List<PortInfo> portInfos = new ArrayList();
        if (bo.getInstancePortPOS() != null) {
            for (InstancePortPO portPO : bo.getInstancePortPOS()) {
                if (portPO == null || portPO.getProtocol() == null || portPO.getPort() == null) {
                    continue;
                }
                portInfos.add(PortInfo.newBuilder()
                        .setProtocol(portPO.getProtocol())
                        .setName(portPO.getName())
                        .setPort(Integer.valueOf(portPO.getPort()))
                        .build());
            }
        }
        InstanceOuterClass.Instance instance = InstanceOuterClass.Instance.newBuilder()
                .setInstanceId(bo.getInstanceId())
                .setAppCode(bo.getInstancePO().getAppCode())
                .setIp(bo.getInstancePO().getIp())
                .setEnvType(bo.getInstancePO().getEnvType())
                .setEnvGroup(bo.getInstancePO().getEnvGroup())
                .setEnvCode(bo.getInstancePO().getEnvCode())
                .setCluster(bo.getInstancePO().getCluster())
                .setVersion(bo.getInstancePO().getVersion())
                .setReversion(bo.getInstancePO().getReversion())
                .setProvider(bo.getInstancePO().getProvider())
                .setCpu(bo.getInstancePO().getCpu())
                .setMemory(bo.getInstancePO().getMemory())
                .setDisk(bo.getInstancePO().getDisk())
                .setOs(bo.getInstancePO().getOs())
                .putAllImage(image)
                .putAllLabel(label)
                .setHostname(bo.getInstancePO().getHostname())
                .setIdc(bo.getInstancePO().getIdc())
                .setEnabled(InstanceEnableEnum.ENABLE.getCode().equals(bo.getInstancePO().getEnabled()))
                .setState(bo.getInstancePO().getState())
                .setHealthState(bo.getInstancePO().getHealthState())
                .setStatus(bo.getInstancePO().getStatus())
                .addAllPorts(portInfos)
                .build();
        return instance;
    }

    public static InstanceBO toInstanceBO(InstancePO instancePO, List<InstancePortPO> instancePortPOS) {
        return InstanceBO.builder()
                .instanceId(instancePO.getInstanceId())
                .instancePO(instancePO)
                .instancePortPOS(instancePortPOS)
                .build();
    }


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

    private static InstanceLbBO toInstanceLbBO(InstancePO instancePO) {
        return InstanceLbBO.builder()
                .instanceId(instancePO.getInstanceId())
                .instancePO(instancePO)
                .weight(1)
                .build();
    }

    public static InstanceLbBO toInstanceLbWeightBO(InstancePO instancePO, Integer weight) {
        return InstanceLbBO.builder()
                .instanceId(instancePO.getInstanceId())
                .instancePO(instancePO)
                .weight(weight)
                .build();
    }

    public static List<InstanceLbBO> toInstanceLbBOList(List<InstancePO> instancePOS) {
        List<InstanceLbBO> lbBoList = new ArrayList<>();
        instancePOS.forEach((po) -> {
            InstanceLbBO bo = toInstanceLbBO(po);
            BeanUtils.copyProperties(po, bo);
            lbBoList.add(bo);
        });
        return lbBoList;
    }


    public static List<InstanceKubsDTO> toInstanceOuterList(List<InstancePO> instancePOS,
            List<InstancePortPO> instancePortPOS) {

        Map<String, List<InstancePortPO>> instancePortMap = instancePortPOS.stream()
                .collect(Collectors.groupingBy(InstancePortPO::getInstanceId));

        List<InstanceKubsDTO> instanceList = new ArrayList<>();

        instancePOS.forEach((po) -> {
            if(!CollectionUtils.isEmpty(instancePortMap.get(po.getInstanceId()))){
                instanceList.add(toInstanceKubsDTO(po,instancePortMap.get(po.getInstanceId())));
            }
        });

        return instanceList;
    }

    public static InstanceKubsDTO toInstanceKubsDTO(InstancePO po,List<InstancePortPO> instancePortPOS){
        InstanceKubsDTO result = InstanceKubsDTO.builder().build();
        BeanUtils.copyProperties(po, result);
        result.setEnabled(InstanceEnableEnum.ENABLE.getCode().equals(po.getEnabled()));
        if (StringUtils.isNotEmpty(po.getImage())) {
            Map<String, String> image = GsonUtils.fromJson(po.getImage(), Map.class);
            result.setImage(image);
        }
        if (StringUtils.isNotEmpty(po.getLabel())) {
            Map<String, String> label = GsonUtils.fromJson(po.getLabel(), Map.class);
            result.setLabel(label);
        }

        List<InstanceKubsPortDTO> ports = new ArrayList<>();
        for(InstancePortPO instancePortPO : instancePortPOS){
            InstanceKubsPortDTO port = InstanceKubsPortDTO.builder()
                    .port(Integer.parseInt(instancePortPO.getPort()))
                    .name(instancePortPO.getName())
                    .protocol(instancePortPO.getProtocol())
                    .build();
            ports.add(port);
        }

        result.setPorts(ports);
        return result;
    }

    public static List<InstanceOnlineDTO> toInstanceOnlineDTOS(List<InstancePO> defaultVersion) {
        if(CollectionUtils.isEmpty(defaultVersion)){
            return new ArrayList<>();
        }

        List<InstanceOnlineDTO> instanceOnlineDTOS = new ArrayList<>();

        if (!CollectionUtils.isEmpty(defaultVersion)) {
            defaultVersion.forEach(instancePO -> instanceOnlineDTOS.add(toOnlineDTO(instancePO)));
        }
        return instanceOnlineDTOS;
    }

    public static InstanceOnlineDTO toOnlineDTO(InstancePO instancePO) {
        InstanceOnlineDTO instanceOnlineDTO = new InstanceOnlineDTO();
        if (instancePO != null) {
            BeanUtils.copyProperties(instancePO, instanceOnlineDTO);
        }
        return instanceOnlineDTO;
    }

    public static GetAllInstancesRequestDTO toAllInstancesRequestDTO(GetAllInstancesRequest request) {
        GetAllInstancesRequestDTO requestDTO = new GetAllInstancesRequestDTO();
        requestDTO.setStatus(request.getStatus());
        if (StringUtils.isNotEmpty(request.getProvider())) {
            requestDTO.setProvider(request.getProvider());
        }
        return requestDTO;
    }
}
