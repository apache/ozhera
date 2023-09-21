package com.xiaomi.hera.trace.etl.consumer;

import com.google.common.util.concurrent.Monitor;
import com.xiaomi.hera.trace.etl.api.service.IEnterManager;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * @author goodjava@qq.com
 * @date 2023/8/31 13:49
 */
@Service
public class EnterManager implements IEnterManager {

    @Getter
    private Monitor monitor = new Monitor();

    private AtomicInteger processNum = new AtomicInteger();

    private Monitor.Guard guard = monitor.newGuard(new BooleanSupplier() {
        @Override
        public boolean getAsBoolean() {
            return false;
        }
    });


    public void enter() {
        monitor.enter();
        monitor.leave();
    }

    @Override
    public AtomicInteger getProcessNum() {
        return this.processNum;
    }

}
