package org.apache.ozhera.log.manager.service.nacos;

import lombok.Data;

import java.util.List;

@Data
public class ManagerLogFilterConfig {

    private List<Long> tailIdList;

    private Boolean enableGlobalFilter;

    private List<String> logLevelList;

}
