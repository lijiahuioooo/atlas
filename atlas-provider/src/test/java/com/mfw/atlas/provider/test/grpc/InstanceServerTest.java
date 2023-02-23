package com.mfw.atlas.provider.test.grpc;

import com.mfw.atlas.provider.grpc.InstanceOuterClass;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.CommonResponse;
import com.mfw.atlas.provider.grpc.InstanceServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InstanceServerTest {

    private InstanceServiceGrpc.InstanceServiceBlockingStub blockingStub;

    public InstanceServerTest(ManagedChannel channel) {
        blockingStub = InstanceServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public void InstanceServerTest(Channel channel) {
        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = InstanceServiceGrpc.newBlockingStub(channel);
    }

    private static final Metadata.Key<CommonResponse> ERROR_INFO_TRAILER_KEY =
            ProtoUtils.keyForProto(CommonResponse.getDefaultInstance());
    /**
     * Say hello to server.
     */
    public void request(String name) {
        log.info("try to request " + name + " ...");
        InstanceOuterClass.SynInstancesRequest request = InstanceOuterClass.SynInstancesRequest.newBuilder().build();
        CommonResponse response;
        try {
            response = blockingStub.synInstance(request);
//            response.getInitializationErrorString();
            System.out.println(response.getInitializationErrorString());
        } catch (StatusRuntimeException e) {

            if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                Metadata trailers = Status.trailersFromThrowable(e);
                if (trailers.containsKey(ERROR_INFO_TRAILER_KEY)) {
                    CommonResponse errorInfo = trailers.get(ERROR_INFO_TRAILER_KEY);
                    if (errorInfo.getCode() == 1111111) {
                        // 这就是我们想要的自定义异常的信息
                        System.out.println(errorInfo.getMsg());
                    }
                }
            } else {
                throw e;
            }
            log.error("error ", e);
            return;
        }
        log.info("success: " + response.toString());
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        try {
            InstanceServerTest client = new InstanceServerTest(channel);
            client.request("dddd");
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}



