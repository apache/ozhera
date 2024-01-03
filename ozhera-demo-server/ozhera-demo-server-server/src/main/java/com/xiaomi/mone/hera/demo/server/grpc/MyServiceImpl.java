package com.xiaomi.mone.hera.demo.server.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import com.xiaomi.mone.hera.demo.grpc.HelloRequest;
import com.xiaomi.mone.hera.demo.grpc.MyServiceGrpc;
import com.xiaomi.mone.hera.demo.grpc.HelloReply;

import java.util.concurrent.TimeUnit;

@GrpcService
public class MyServiceImpl extends MyServiceGrpc.MyServiceImplBase {

    @Override
    public void normal(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void slow(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .build();
        try {
            TimeUnit.SECONDS.sleep(1);
        }catch (Throwable t){

        }
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void error(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .build();
        if(true) {
            throw new RuntimeException("error");
        }
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
