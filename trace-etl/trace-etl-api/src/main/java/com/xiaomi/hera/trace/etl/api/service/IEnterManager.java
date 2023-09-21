package com.xiaomi.hera.trace.etl.api.service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author goodjava@qq.com
 * @date 2023/9/19 17:08
 */
public interface IEnterManager  {

    AtomicInteger processNum = new AtomicInteger();

    void enter();

    AtomicInteger getProcessNum();

}
