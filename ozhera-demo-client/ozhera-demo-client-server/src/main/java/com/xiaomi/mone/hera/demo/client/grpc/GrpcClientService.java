/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
