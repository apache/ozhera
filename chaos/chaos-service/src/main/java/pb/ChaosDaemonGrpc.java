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
package pb;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.48.0)",
    comments = "Source: chaosdaemon.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ChaosDaemonGrpc {

  private ChaosDaemonGrpc() {}

  public static final String SERVICE_NAME = "pb.ChaosDaemon";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.TcsRequest,
      com.google.protobuf.Empty> getSetTcsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetTcs",
      requestType = pb.Chaosdaemon.TcsRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.TcsRequest,
      com.google.protobuf.Empty> getSetTcsMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.TcsRequest, com.google.protobuf.Empty> getSetTcsMethod;
    if ((getSetTcsMethod = ChaosDaemonGrpc.getSetTcsMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getSetTcsMethod = ChaosDaemonGrpc.getSetTcsMethod) == null) {
          ChaosDaemonGrpc.getSetTcsMethod = getSetTcsMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.TcsRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetTcs"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.TcsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("SetTcs"))
              .build();
        }
      }
    }
    return getSetTcsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.IPSetsRequest,
      com.google.protobuf.Empty> getFlushIPSetsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FlushIPSets",
      requestType = pb.Chaosdaemon.IPSetsRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.IPSetsRequest,
      com.google.protobuf.Empty> getFlushIPSetsMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.IPSetsRequest, com.google.protobuf.Empty> getFlushIPSetsMethod;
    if ((getFlushIPSetsMethod = ChaosDaemonGrpc.getFlushIPSetsMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getFlushIPSetsMethod = ChaosDaemonGrpc.getFlushIPSetsMethod) == null) {
          ChaosDaemonGrpc.getFlushIPSetsMethod = getFlushIPSetsMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.IPSetsRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FlushIPSets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.IPSetsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("FlushIPSets"))
              .build();
        }
      }
    }
    return getFlushIPSetsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.IptablesChainsRequest,
      com.google.protobuf.Empty> getSetIptablesChainsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetIptablesChains",
      requestType = pb.Chaosdaemon.IptablesChainsRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.IptablesChainsRequest,
      com.google.protobuf.Empty> getSetIptablesChainsMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.IptablesChainsRequest, com.google.protobuf.Empty> getSetIptablesChainsMethod;
    if ((getSetIptablesChainsMethod = ChaosDaemonGrpc.getSetIptablesChainsMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getSetIptablesChainsMethod = ChaosDaemonGrpc.getSetIptablesChainsMethod) == null) {
          ChaosDaemonGrpc.getSetIptablesChainsMethod = getSetIptablesChainsMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.IptablesChainsRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetIptablesChains"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.IptablesChainsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("SetIptablesChains"))
              .build();
        }
      }
    }
    return getSetIptablesChainsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.TimeRequest,
      com.google.protobuf.Empty> getSetTimeOffsetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetTimeOffset",
      requestType = pb.Chaosdaemon.TimeRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.TimeRequest,
      com.google.protobuf.Empty> getSetTimeOffsetMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.TimeRequest, com.google.protobuf.Empty> getSetTimeOffsetMethod;
    if ((getSetTimeOffsetMethod = ChaosDaemonGrpc.getSetTimeOffsetMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getSetTimeOffsetMethod = ChaosDaemonGrpc.getSetTimeOffsetMethod) == null) {
          ChaosDaemonGrpc.getSetTimeOffsetMethod = getSetTimeOffsetMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.TimeRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetTimeOffset"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.TimeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("SetTimeOffset"))
              .build();
        }
      }
    }
    return getSetTimeOffsetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.TimeRequest,
      com.google.protobuf.Empty> getRecoverTimeOffsetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RecoverTimeOffset",
      requestType = pb.Chaosdaemon.TimeRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.TimeRequest,
      com.google.protobuf.Empty> getRecoverTimeOffsetMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.TimeRequest, com.google.protobuf.Empty> getRecoverTimeOffsetMethod;
    if ((getRecoverTimeOffsetMethod = ChaosDaemonGrpc.getRecoverTimeOffsetMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getRecoverTimeOffsetMethod = ChaosDaemonGrpc.getRecoverTimeOffsetMethod) == null) {
          ChaosDaemonGrpc.getRecoverTimeOffsetMethod = getRecoverTimeOffsetMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.TimeRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RecoverTimeOffset"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.TimeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("RecoverTimeOffset"))
              .build();
        }
      }
    }
    return getRecoverTimeOffsetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.ContainerRequest,
      com.google.protobuf.Empty> getContainerKillMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ContainerKill",
      requestType = pb.Chaosdaemon.ContainerRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.ContainerRequest,
      com.google.protobuf.Empty> getContainerKillMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.ContainerRequest, com.google.protobuf.Empty> getContainerKillMethod;
    if ((getContainerKillMethod = ChaosDaemonGrpc.getContainerKillMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getContainerKillMethod = ChaosDaemonGrpc.getContainerKillMethod) == null) {
          ChaosDaemonGrpc.getContainerKillMethod = getContainerKillMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.ContainerRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ContainerKill"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ContainerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("ContainerKill"))
              .build();
        }
      }
    }
    return getContainerKillMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.ContainerRequest,
      pb.Chaosdaemon.ContainerResponse> getContainerGetPidMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ContainerGetPid",
      requestType = pb.Chaosdaemon.ContainerRequest.class,
      responseType = pb.Chaosdaemon.ContainerResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.ContainerRequest,
      pb.Chaosdaemon.ContainerResponse> getContainerGetPidMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.ContainerRequest, pb.Chaosdaemon.ContainerResponse> getContainerGetPidMethod;
    if ((getContainerGetPidMethod = ChaosDaemonGrpc.getContainerGetPidMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getContainerGetPidMethod = ChaosDaemonGrpc.getContainerGetPidMethod) == null) {
          ChaosDaemonGrpc.getContainerGetPidMethod = getContainerGetPidMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.ContainerRequest, pb.Chaosdaemon.ContainerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ContainerGetPid"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ContainerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ContainerResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("ContainerGetPid"))
              .build();
        }
      }
    }
    return getContainerGetPidMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.ExecStressRequest,
      pb.Chaosdaemon.ExecStressResponse> getExecStressorsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ExecStressors",
      requestType = pb.Chaosdaemon.ExecStressRequest.class,
      responseType = pb.Chaosdaemon.ExecStressResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.ExecStressRequest,
      pb.Chaosdaemon.ExecStressResponse> getExecStressorsMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.ExecStressRequest, pb.Chaosdaemon.ExecStressResponse> getExecStressorsMethod;
    if ((getExecStressorsMethod = ChaosDaemonGrpc.getExecStressorsMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getExecStressorsMethod = ChaosDaemonGrpc.getExecStressorsMethod) == null) {
          ChaosDaemonGrpc.getExecStressorsMethod = getExecStressorsMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.ExecStressRequest, pb.Chaosdaemon.ExecStressResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ExecStressors"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ExecStressRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ExecStressResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("ExecStressors"))
              .build();
        }
      }
    }
    return getExecStressorsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.CancelStressRequest,
      com.google.protobuf.Empty> getCancelStressorsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CancelStressors",
      requestType = pb.Chaosdaemon.CancelStressRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.CancelStressRequest,
      com.google.protobuf.Empty> getCancelStressorsMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.CancelStressRequest, com.google.protobuf.Empty> getCancelStressorsMethod;
    if ((getCancelStressorsMethod = ChaosDaemonGrpc.getCancelStressorsMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getCancelStressorsMethod = ChaosDaemonGrpc.getCancelStressorsMethod) == null) {
          ChaosDaemonGrpc.getCancelStressorsMethod = getCancelStressorsMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.CancelStressRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CancelStressors"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.CancelStressRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("CancelStressors"))
              .build();
        }
      }
    }
    return getCancelStressorsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyIOChaosRequest,
      pb.Chaosdaemon.ApplyIOChaosResponse> getApplyIOChaosMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ApplyIOChaos",
      requestType = pb.Chaosdaemon.ApplyIOChaosRequest.class,
      responseType = pb.Chaosdaemon.ApplyIOChaosResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyIOChaosRequest,
      pb.Chaosdaemon.ApplyIOChaosResponse> getApplyIOChaosMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyIOChaosRequest, pb.Chaosdaemon.ApplyIOChaosResponse> getApplyIOChaosMethod;
    if ((getApplyIOChaosMethod = ChaosDaemonGrpc.getApplyIOChaosMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getApplyIOChaosMethod = ChaosDaemonGrpc.getApplyIOChaosMethod) == null) {
          ChaosDaemonGrpc.getApplyIOChaosMethod = getApplyIOChaosMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.ApplyIOChaosRequest, pb.Chaosdaemon.ApplyIOChaosResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ApplyIOChaos"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ApplyIOChaosRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ApplyIOChaosResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("ApplyIOChaos"))
              .build();
        }
      }
    }
    return getApplyIOChaosMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyHttpChaosRequest,
      pb.Chaosdaemon.ApplyHttpChaosResponse> getApplyHttpChaosMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ApplyHttpChaos",
      requestType = pb.Chaosdaemon.ApplyHttpChaosRequest.class,
      responseType = pb.Chaosdaemon.ApplyHttpChaosResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyHttpChaosRequest,
      pb.Chaosdaemon.ApplyHttpChaosResponse> getApplyHttpChaosMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyHttpChaosRequest, pb.Chaosdaemon.ApplyHttpChaosResponse> getApplyHttpChaosMethod;
    if ((getApplyHttpChaosMethod = ChaosDaemonGrpc.getApplyHttpChaosMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getApplyHttpChaosMethod = ChaosDaemonGrpc.getApplyHttpChaosMethod) == null) {
          ChaosDaemonGrpc.getApplyHttpChaosMethod = getApplyHttpChaosMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.ApplyHttpChaosRequest, pb.Chaosdaemon.ApplyHttpChaosResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ApplyHttpChaos"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ApplyHttpChaosRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ApplyHttpChaosResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("ApplyHttpChaos"))
              .build();
        }
      }
    }
    return getApplyHttpChaosMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyBlockChaosRequest,
      pb.Chaosdaemon.ApplyBlockChaosResponse> getApplyBlockChaosMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ApplyBlockChaos",
      requestType = pb.Chaosdaemon.ApplyBlockChaosRequest.class,
      responseType = pb.Chaosdaemon.ApplyBlockChaosResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyBlockChaosRequest,
      pb.Chaosdaemon.ApplyBlockChaosResponse> getApplyBlockChaosMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.ApplyBlockChaosRequest, pb.Chaosdaemon.ApplyBlockChaosResponse> getApplyBlockChaosMethod;
    if ((getApplyBlockChaosMethod = ChaosDaemonGrpc.getApplyBlockChaosMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getApplyBlockChaosMethod = ChaosDaemonGrpc.getApplyBlockChaosMethod) == null) {
          ChaosDaemonGrpc.getApplyBlockChaosMethod = getApplyBlockChaosMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.ApplyBlockChaosRequest, pb.Chaosdaemon.ApplyBlockChaosResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ApplyBlockChaos"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ApplyBlockChaosRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.ApplyBlockChaosResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("ApplyBlockChaos"))
              .build();
        }
      }
    }
    return getApplyBlockChaosMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.RecoverBlockChaosRequest,
      com.google.protobuf.Empty> getRecoverBlockChaosMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RecoverBlockChaos",
      requestType = pb.Chaosdaemon.RecoverBlockChaosRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.RecoverBlockChaosRequest,
      com.google.protobuf.Empty> getRecoverBlockChaosMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.RecoverBlockChaosRequest, com.google.protobuf.Empty> getRecoverBlockChaosMethod;
    if ((getRecoverBlockChaosMethod = ChaosDaemonGrpc.getRecoverBlockChaosMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getRecoverBlockChaosMethod = ChaosDaemonGrpc.getRecoverBlockChaosMethod) == null) {
          ChaosDaemonGrpc.getRecoverBlockChaosMethod = getRecoverBlockChaosMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.RecoverBlockChaosRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RecoverBlockChaos"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.RecoverBlockChaosRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("RecoverBlockChaos"))
              .build();
        }
      }
    }
    return getRecoverBlockChaosMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.SetDNSServerRequest,
      com.google.protobuf.Empty> getSetDNSServerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetDNSServer",
      requestType = pb.Chaosdaemon.SetDNSServerRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.SetDNSServerRequest,
      com.google.protobuf.Empty> getSetDNSServerMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.SetDNSServerRequest, com.google.protobuf.Empty> getSetDNSServerMethod;
    if ((getSetDNSServerMethod = ChaosDaemonGrpc.getSetDNSServerMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getSetDNSServerMethod = ChaosDaemonGrpc.getSetDNSServerMethod) == null) {
          ChaosDaemonGrpc.getSetDNSServerMethod = getSetDNSServerMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.SetDNSServerRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetDNSServer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.SetDNSServerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("SetDNSServer"))
              .build();
        }
      }
    }
    return getSetDNSServerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.InstallJVMRulesRequest,
      com.google.protobuf.Empty> getInstallJVMRulesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "InstallJVMRules",
      requestType = pb.Chaosdaemon.InstallJVMRulesRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.InstallJVMRulesRequest,
      com.google.protobuf.Empty> getInstallJVMRulesMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.InstallJVMRulesRequest, com.google.protobuf.Empty> getInstallJVMRulesMethod;
    if ((getInstallJVMRulesMethod = ChaosDaemonGrpc.getInstallJVMRulesMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getInstallJVMRulesMethod = ChaosDaemonGrpc.getInstallJVMRulesMethod) == null) {
          ChaosDaemonGrpc.getInstallJVMRulesMethod = getInstallJVMRulesMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.InstallJVMRulesRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "InstallJVMRules"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.InstallJVMRulesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("InstallJVMRules"))
              .build();
        }
      }
    }
    return getInstallJVMRulesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pb.Chaosdaemon.UninstallJVMRulesRequest,
      com.google.protobuf.Empty> getUninstallJVMRulesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UninstallJVMRules",
      requestType = pb.Chaosdaemon.UninstallJVMRulesRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pb.Chaosdaemon.UninstallJVMRulesRequest,
      com.google.protobuf.Empty> getUninstallJVMRulesMethod() {
    io.grpc.MethodDescriptor<pb.Chaosdaemon.UninstallJVMRulesRequest, com.google.protobuf.Empty> getUninstallJVMRulesMethod;
    if ((getUninstallJVMRulesMethod = ChaosDaemonGrpc.getUninstallJVMRulesMethod) == null) {
      synchronized (ChaosDaemonGrpc.class) {
        if ((getUninstallJVMRulesMethod = ChaosDaemonGrpc.getUninstallJVMRulesMethod) == null) {
          ChaosDaemonGrpc.getUninstallJVMRulesMethod = getUninstallJVMRulesMethod =
              io.grpc.MethodDescriptor.<pb.Chaosdaemon.UninstallJVMRulesRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UninstallJVMRules"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pb.Chaosdaemon.UninstallJVMRulesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new ChaosDaemonMethodDescriptorSupplier("UninstallJVMRules"))
              .build();
        }
      }
    }
    return getUninstallJVMRulesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ChaosDaemonStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChaosDaemonStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChaosDaemonStub>() {
        @java.lang.Override
        public ChaosDaemonStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChaosDaemonStub(channel, callOptions);
        }
      };
    return ChaosDaemonStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ChaosDaemonBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChaosDaemonBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChaosDaemonBlockingStub>() {
        @java.lang.Override
        public ChaosDaemonBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChaosDaemonBlockingStub(channel, callOptions);
        }
      };
    return ChaosDaemonBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ChaosDaemonFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ChaosDaemonFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ChaosDaemonFutureStub>() {
        @java.lang.Override
        public ChaosDaemonFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ChaosDaemonFutureStub(channel, callOptions);
        }
      };
    return ChaosDaemonFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class ChaosDaemonImplBase implements io.grpc.BindableService {

    /**
     */
    public void setTcs(pb.Chaosdaemon.TcsRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetTcsMethod(), responseObserver);
    }

    /**
     */
    public void flushIPSets(pb.Chaosdaemon.IPSetsRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFlushIPSetsMethod(), responseObserver);
    }

    /**
     */
    public void setIptablesChains(pb.Chaosdaemon.IptablesChainsRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetIptablesChainsMethod(), responseObserver);
    }

    /**
     */
    public void setTimeOffset(pb.Chaosdaemon.TimeRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetTimeOffsetMethod(), responseObserver);
    }

    /**
     */
    public void recoverTimeOffset(pb.Chaosdaemon.TimeRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRecoverTimeOffsetMethod(), responseObserver);
    }

    /**
     */
    public void containerKill(pb.Chaosdaemon.ContainerRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getContainerKillMethod(), responseObserver);
    }

    /**
     */
    public void containerGetPid(pb.Chaosdaemon.ContainerRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ContainerResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getContainerGetPidMethod(), responseObserver);
    }

    /**
     */
    public void execStressors(pb.Chaosdaemon.ExecStressRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ExecStressResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExecStressorsMethod(), responseObserver);
    }

    /**
     */
    public void cancelStressors(pb.Chaosdaemon.CancelStressRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCancelStressorsMethod(), responseObserver);
    }

    /**
     */
    public void applyIOChaos(pb.Chaosdaemon.ApplyIOChaosRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyIOChaosResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getApplyIOChaosMethod(), responseObserver);
    }

    /**
     */
    public void applyHttpChaos(pb.Chaosdaemon.ApplyHttpChaosRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyHttpChaosResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getApplyHttpChaosMethod(), responseObserver);
    }

    /**
     */
    public void applyBlockChaos(pb.Chaosdaemon.ApplyBlockChaosRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyBlockChaosResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getApplyBlockChaosMethod(), responseObserver);
    }

    /**
     */
    public void recoverBlockChaos(pb.Chaosdaemon.RecoverBlockChaosRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRecoverBlockChaosMethod(), responseObserver);
    }

    /**
     */
    public void setDNSServer(pb.Chaosdaemon.SetDNSServerRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetDNSServerMethod(), responseObserver);
    }

    /**
     */
    public void installJVMRules(pb.Chaosdaemon.InstallJVMRulesRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getInstallJVMRulesMethod(), responseObserver);
    }

    /**
     */
    public void uninstallJVMRules(pb.Chaosdaemon.UninstallJVMRulesRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUninstallJVMRulesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSetTcsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.TcsRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_SET_TCS)))
          .addMethod(
            getFlushIPSetsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.IPSetsRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_FLUSH_IPSETS)))
          .addMethod(
            getSetIptablesChainsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.IptablesChainsRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_SET_IPTABLES_CHAINS)))
          .addMethod(
            getSetTimeOffsetMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.TimeRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_SET_TIME_OFFSET)))
          .addMethod(
            getRecoverTimeOffsetMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.TimeRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_RECOVER_TIME_OFFSET)))
          .addMethod(
            getContainerKillMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.ContainerRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_CONTAINER_KILL)))
          .addMethod(
            getContainerGetPidMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.ContainerRequest,
                pb.Chaosdaemon.ContainerResponse>(
                  this, METHODID_CONTAINER_GET_PID)))
          .addMethod(
            getExecStressorsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.ExecStressRequest,
                pb.Chaosdaemon.ExecStressResponse>(
                  this, METHODID_EXEC_STRESSORS)))
          .addMethod(
            getCancelStressorsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.CancelStressRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_CANCEL_STRESSORS)))
          .addMethod(
            getApplyIOChaosMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.ApplyIOChaosRequest,
                pb.Chaosdaemon.ApplyIOChaosResponse>(
                  this, METHODID_APPLY_IOCHAOS)))
          .addMethod(
            getApplyHttpChaosMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.ApplyHttpChaosRequest,
                pb.Chaosdaemon.ApplyHttpChaosResponse>(
                  this, METHODID_APPLY_HTTP_CHAOS)))
          .addMethod(
            getApplyBlockChaosMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.ApplyBlockChaosRequest,
                pb.Chaosdaemon.ApplyBlockChaosResponse>(
                  this, METHODID_APPLY_BLOCK_CHAOS)))
          .addMethod(
            getRecoverBlockChaosMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.RecoverBlockChaosRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_RECOVER_BLOCK_CHAOS)))
          .addMethod(
            getSetDNSServerMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.SetDNSServerRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_SET_DNSSERVER)))
          .addMethod(
            getInstallJVMRulesMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.InstallJVMRulesRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_INSTALL_JVMRULES)))
          .addMethod(
            getUninstallJVMRulesMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pb.Chaosdaemon.UninstallJVMRulesRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_UNINSTALL_JVMRULES)))
          .build();
    }
  }

  /**
   */
  public static final class ChaosDaemonStub extends io.grpc.stub.AbstractAsyncStub<ChaosDaemonStub> {
    private ChaosDaemonStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaosDaemonStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChaosDaemonStub(channel, callOptions);
    }

    /**
     */
    public void setTcs(pb.Chaosdaemon.TcsRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetTcsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void flushIPSets(pb.Chaosdaemon.IPSetsRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFlushIPSetsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setIptablesChains(pb.Chaosdaemon.IptablesChainsRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetIptablesChainsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setTimeOffset(pb.Chaosdaemon.TimeRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetTimeOffsetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void recoverTimeOffset(pb.Chaosdaemon.TimeRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRecoverTimeOffsetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void containerKill(pb.Chaosdaemon.ContainerRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getContainerKillMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void containerGetPid(pb.Chaosdaemon.ContainerRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ContainerResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getContainerGetPidMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void execStressors(pb.Chaosdaemon.ExecStressRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ExecStressResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExecStressorsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void cancelStressors(pb.Chaosdaemon.CancelStressRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCancelStressorsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void applyIOChaos(pb.Chaosdaemon.ApplyIOChaosRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyIOChaosResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getApplyIOChaosMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void applyHttpChaos(pb.Chaosdaemon.ApplyHttpChaosRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyHttpChaosResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getApplyHttpChaosMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void applyBlockChaos(pb.Chaosdaemon.ApplyBlockChaosRequest request,
        io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyBlockChaosResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getApplyBlockChaosMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void recoverBlockChaos(pb.Chaosdaemon.RecoverBlockChaosRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRecoverBlockChaosMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setDNSServer(pb.Chaosdaemon.SetDNSServerRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetDNSServerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void installJVMRules(pb.Chaosdaemon.InstallJVMRulesRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getInstallJVMRulesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void uninstallJVMRules(pb.Chaosdaemon.UninstallJVMRulesRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUninstallJVMRulesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ChaosDaemonBlockingStub extends io.grpc.stub.AbstractBlockingStub<ChaosDaemonBlockingStub> {
    private ChaosDaemonBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaosDaemonBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChaosDaemonBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.google.protobuf.Empty setTcs(pb.Chaosdaemon.TcsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetTcsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty flushIPSets(pb.Chaosdaemon.IPSetsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFlushIPSetsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty setIptablesChains(pb.Chaosdaemon.IptablesChainsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetIptablesChainsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty setTimeOffset(pb.Chaosdaemon.TimeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetTimeOffsetMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty recoverTimeOffset(pb.Chaosdaemon.TimeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRecoverTimeOffsetMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty containerKill(pb.Chaosdaemon.ContainerRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getContainerKillMethod(), getCallOptions(), request);
    }

    /**
     */
    public pb.Chaosdaemon.ContainerResponse containerGetPid(pb.Chaosdaemon.ContainerRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getContainerGetPidMethod(), getCallOptions(), request);
    }

    /**
     */
    public pb.Chaosdaemon.ExecStressResponse execStressors(pb.Chaosdaemon.ExecStressRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExecStressorsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty cancelStressors(pb.Chaosdaemon.CancelStressRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCancelStressorsMethod(), getCallOptions(), request);
    }

    /**
     */
    public pb.Chaosdaemon.ApplyIOChaosResponse applyIOChaos(pb.Chaosdaemon.ApplyIOChaosRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getApplyIOChaosMethod(), getCallOptions(), request);
    }

    /**
     */
    public pb.Chaosdaemon.ApplyHttpChaosResponse applyHttpChaos(pb.Chaosdaemon.ApplyHttpChaosRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getApplyHttpChaosMethod(), getCallOptions(), request);
    }

    /**
     */
    public pb.Chaosdaemon.ApplyBlockChaosResponse applyBlockChaos(pb.Chaosdaemon.ApplyBlockChaosRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getApplyBlockChaosMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty recoverBlockChaos(pb.Chaosdaemon.RecoverBlockChaosRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRecoverBlockChaosMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty setDNSServer(pb.Chaosdaemon.SetDNSServerRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetDNSServerMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty installJVMRules(pb.Chaosdaemon.InstallJVMRulesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getInstallJVMRulesMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty uninstallJVMRules(pb.Chaosdaemon.UninstallJVMRulesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUninstallJVMRulesMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ChaosDaemonFutureStub extends io.grpc.stub.AbstractFutureStub<ChaosDaemonFutureStub> {
    private ChaosDaemonFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChaosDaemonFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ChaosDaemonFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> setTcs(
        pb.Chaosdaemon.TcsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetTcsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> flushIPSets(
        pb.Chaosdaemon.IPSetsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFlushIPSetsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> setIptablesChains(
        pb.Chaosdaemon.IptablesChainsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetIptablesChainsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> setTimeOffset(
        pb.Chaosdaemon.TimeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetTimeOffsetMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> recoverTimeOffset(
        pb.Chaosdaemon.TimeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRecoverTimeOffsetMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> containerKill(
        pb.Chaosdaemon.ContainerRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getContainerKillMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pb.Chaosdaemon.ContainerResponse> containerGetPid(
        pb.Chaosdaemon.ContainerRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getContainerGetPidMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pb.Chaosdaemon.ExecStressResponse> execStressors(
        pb.Chaosdaemon.ExecStressRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExecStressorsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> cancelStressors(
        pb.Chaosdaemon.CancelStressRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCancelStressorsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pb.Chaosdaemon.ApplyIOChaosResponse> applyIOChaos(
        pb.Chaosdaemon.ApplyIOChaosRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getApplyIOChaosMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pb.Chaosdaemon.ApplyHttpChaosResponse> applyHttpChaos(
        pb.Chaosdaemon.ApplyHttpChaosRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getApplyHttpChaosMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pb.Chaosdaemon.ApplyBlockChaosResponse> applyBlockChaos(
        pb.Chaosdaemon.ApplyBlockChaosRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getApplyBlockChaosMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> recoverBlockChaos(
        pb.Chaosdaemon.RecoverBlockChaosRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRecoverBlockChaosMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> setDNSServer(
        pb.Chaosdaemon.SetDNSServerRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetDNSServerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> installJVMRules(
        pb.Chaosdaemon.InstallJVMRulesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getInstallJVMRulesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> uninstallJVMRules(
        pb.Chaosdaemon.UninstallJVMRulesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUninstallJVMRulesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SET_TCS = 0;
  private static final int METHODID_FLUSH_IPSETS = 1;
  private static final int METHODID_SET_IPTABLES_CHAINS = 2;
  private static final int METHODID_SET_TIME_OFFSET = 3;
  private static final int METHODID_RECOVER_TIME_OFFSET = 4;
  private static final int METHODID_CONTAINER_KILL = 5;
  private static final int METHODID_CONTAINER_GET_PID = 6;
  private static final int METHODID_EXEC_STRESSORS = 7;
  private static final int METHODID_CANCEL_STRESSORS = 8;
  private static final int METHODID_APPLY_IOCHAOS = 9;
  private static final int METHODID_APPLY_HTTP_CHAOS = 10;
  private static final int METHODID_APPLY_BLOCK_CHAOS = 11;
  private static final int METHODID_RECOVER_BLOCK_CHAOS = 12;
  private static final int METHODID_SET_DNSSERVER = 13;
  private static final int METHODID_INSTALL_JVMRULES = 14;
  private static final int METHODID_UNINSTALL_JVMRULES = 15;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ChaosDaemonImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ChaosDaemonImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SET_TCS:
          serviceImpl.setTcs((pb.Chaosdaemon.TcsRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_FLUSH_IPSETS:
          serviceImpl.flushIPSets((pb.Chaosdaemon.IPSetsRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_SET_IPTABLES_CHAINS:
          serviceImpl.setIptablesChains((pb.Chaosdaemon.IptablesChainsRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_SET_TIME_OFFSET:
          serviceImpl.setTimeOffset((pb.Chaosdaemon.TimeRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_RECOVER_TIME_OFFSET:
          serviceImpl.recoverTimeOffset((pb.Chaosdaemon.TimeRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_CONTAINER_KILL:
          serviceImpl.containerKill((pb.Chaosdaemon.ContainerRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_CONTAINER_GET_PID:
          serviceImpl.containerGetPid((pb.Chaosdaemon.ContainerRequest) request,
              (io.grpc.stub.StreamObserver<pb.Chaosdaemon.ContainerResponse>) responseObserver);
          break;
        case METHODID_EXEC_STRESSORS:
          serviceImpl.execStressors((pb.Chaosdaemon.ExecStressRequest) request,
              (io.grpc.stub.StreamObserver<pb.Chaosdaemon.ExecStressResponse>) responseObserver);
          break;
        case METHODID_CANCEL_STRESSORS:
          serviceImpl.cancelStressors((pb.Chaosdaemon.CancelStressRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_APPLY_IOCHAOS:
          serviceImpl.applyIOChaos((pb.Chaosdaemon.ApplyIOChaosRequest) request,
              (io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyIOChaosResponse>) responseObserver);
          break;
        case METHODID_APPLY_HTTP_CHAOS:
          serviceImpl.applyHttpChaos((pb.Chaosdaemon.ApplyHttpChaosRequest) request,
              (io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyHttpChaosResponse>) responseObserver);
          break;
        case METHODID_APPLY_BLOCK_CHAOS:
          serviceImpl.applyBlockChaos((pb.Chaosdaemon.ApplyBlockChaosRequest) request,
              (io.grpc.stub.StreamObserver<pb.Chaosdaemon.ApplyBlockChaosResponse>) responseObserver);
          break;
        case METHODID_RECOVER_BLOCK_CHAOS:
          serviceImpl.recoverBlockChaos((pb.Chaosdaemon.RecoverBlockChaosRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_SET_DNSSERVER:
          serviceImpl.setDNSServer((pb.Chaosdaemon.SetDNSServerRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_INSTALL_JVMRULES:
          serviceImpl.installJVMRules((pb.Chaosdaemon.InstallJVMRulesRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_UNINSTALL_JVMRULES:
          serviceImpl.uninstallJVMRules((pb.Chaosdaemon.UninstallJVMRulesRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ChaosDaemonBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ChaosDaemonBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return pb.Chaosdaemon.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ChaosDaemon");
    }
  }

  private static final class ChaosDaemonFileDescriptorSupplier
      extends ChaosDaemonBaseDescriptorSupplier {
    ChaosDaemonFileDescriptorSupplier() {}
  }

  private static final class ChaosDaemonMethodDescriptorSupplier
      extends ChaosDaemonBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ChaosDaemonMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ChaosDaemonGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ChaosDaemonFileDescriptorSupplier())
              .addMethod(getSetTcsMethod())
              .addMethod(getFlushIPSetsMethod())
              .addMethod(getSetIptablesChainsMethod())
              .addMethod(getSetTimeOffsetMethod())
              .addMethod(getRecoverTimeOffsetMethod())
              .addMethod(getContainerKillMethod())
              .addMethod(getContainerGetPidMethod())
              .addMethod(getExecStressorsMethod())
              .addMethod(getCancelStressorsMethod())
              .addMethod(getApplyIOChaosMethod())
              .addMethod(getApplyHttpChaosMethod())
              .addMethod(getApplyBlockChaosMethod())
              .addMethod(getRecoverBlockChaosMethod())
              .addMethod(getSetDNSServerMethod())
              .addMethod(getInstallJVMRulesMethod())
              .addMethod(getUninstallJVMRulesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
