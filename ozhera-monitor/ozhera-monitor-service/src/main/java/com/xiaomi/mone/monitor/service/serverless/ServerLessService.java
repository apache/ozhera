package com.xiaomi.mone.monitor.service.serverless;

import java.util.List;

public interface ServerLessService {

    List<String> getFaasFunctionList(Integer appId);

}
