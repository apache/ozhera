package com.xiaomi.mone.hera.demo.client.api.service;


import run.mone.common.Result;

public interface DubboHealthService {

    int remoteHealth(int size);

    void remoteHealth2();

    Result testResultCode500();

}