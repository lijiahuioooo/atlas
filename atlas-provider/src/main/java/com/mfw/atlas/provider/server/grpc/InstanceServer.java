package com.mfw.atlas.provider.server.grpc;

import com.mfw.atlas.provider.grpc.InstanceOuterClass.CommonResponse;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.GetAllInstancesRequest;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.Instance;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.InstanceList;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.SynAllInstancesRequest;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.SynInstancesRequest;
import com.mfw.atlas.provider.grpc.InstanceServiceGrpc;
import com.mfw.atlas.provider.service.InstanceChangeService;
import com.mfw.atlas.provider.util.GsonUtils;
import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author KL
 * @Time 2020/10/28 3:00 下午
 */
@Slf4j
@GrpcService
public class InstanceServer extends InstanceServiceGrpc.InstanceServiceImplBase {

    @Autowired
    private InstanceChangeService instanceChangeService;

    /**
     * 同步所有实例
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void synAllInstance(SynAllInstancesRequest request,StreamObserver<CommonResponse> responseObserver) {
        long startTime = System.currentTimeMillis();
        try {
            instanceChangeService.synAllInstance(request);
            responseObserver.onNext(CommonResponse.newBuilder().setCode(0).setMsg("ok").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("synAllInstance error",e);
            responseObserver.onNext(CommonResponse.newBuilder().setCode(1).setMsg(e.getMessage()).build());
            responseObserver.onCompleted();
        }
        log.info("synAllInstance size:{},cost:{}", request.getInstanceList().size(), System.currentTimeMillis() - startTime);
    }


    /**
     * 增量更新实例
     *
     * @param request
     * @return
     */
    @Override
    public void synInstance(SynInstancesRequest request, StreamObserver<CommonResponse> responseObserver) {
        long startTime = System.currentTimeMillis();
        try {
            instanceChangeService.synInstance(request);
            responseObserver.onNext(CommonResponse.newBuilder().setCode(0).setMsg("ok").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("synInstance error , param: "+ GsonUtils.toJsonString(request.getInstanceList()),e);
            responseObserver.onNext(CommonResponse.newBuilder().setCode(1).setMsg(e.getMessage()).build());
            responseObserver.onCompleted();
        }
        long endTime = System.currentTimeMillis();
        log.info("synInstance param: {} , cost: {}", request.toString(), endTime - startTime);
    }

    /**
     * 信息返回不完整，仅限数据状态比对使用
     * @param request
     * @param responseObserver
     */
    @Override
    public void getAllInstance(GetAllInstancesRequest request,StreamObserver<InstanceList> responseObserver) {
        try {
            List<Instance> BOList= instanceChangeService.getAllInstance(request);
            responseObserver.onNext(InstanceList.newBuilder().addAllInstance(BOList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("getAllInstance error",e);
            responseObserver.onCompleted();
        }
    }

}
