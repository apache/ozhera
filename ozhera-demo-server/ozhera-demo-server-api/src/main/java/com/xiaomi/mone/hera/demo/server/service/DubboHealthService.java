package com.xiaomi.mone.hera.demo.server.service;


import run.mone.common.Result;

public interface DubboHealthService {

    Result health() throws InterruptedException;

    String simple(int size);

    Result testResultCode500();
}