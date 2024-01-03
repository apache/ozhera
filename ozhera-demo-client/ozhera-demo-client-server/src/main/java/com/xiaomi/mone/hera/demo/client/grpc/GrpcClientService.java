package com.xiaomi.mone.hera.demo.client.grpc;


import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import com.xiaomi.mone.hera.demo.grpc.MyServiceGrpc;
import com.xiaomi.mone.hera.demo.grpc.HelloRequest;

@Service
public class GrpcClientService {

    @GrpcClient("myService")
    private MyServiceGrpc.MyServiceBlockingStub myServiceStub;

    public String grpcNormal(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        return myServiceStub.normal(request).getMessage();
    }

    public String grpcSlow(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        return myServiceStub.slow(request).getMessage();
    }

    public String grpcError(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        return myServiceStub.error(request).getMessage();
    }

}
