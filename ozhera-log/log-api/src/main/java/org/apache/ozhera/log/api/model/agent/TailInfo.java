package org.apache.ozhera.log.api.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TailInfo implements Serializable {

    private Long id;

    private Long spaceId;

    private Long storeId;

    private Long milogAppId;

    private Long appId;

    private String appName;

    private Long envId;

    private String envName;

    private List<String> ips;

    private String tail;

    private Integer parseType;

    private String parseScript;

    private String logPath;

    private String valueList;

    private String tailRate;

    private Long ctime;

    private Long utime;

    private String logSplitExpress;

    private String firstLineReg;

    private Integer appType;

    private Integer machineType;

    private List<?> motorRooms;

    private List<?> middlewareConfig;

    private Integer deployWay;

    private Integer batchSendSize = 20;

    private Boolean collectionReady = true;

    private String source;

    private UserInfo userInfo;

    public String isValidParam(boolean isCreate) {
        if (spaceId == null || spaceId < 0) {
            return "The space id cannot be empty!";
        }
        if (storeId == null || storeId < 0) {
            return "The store id cannot be empty!";
        }
        if (tail == null || tail.isEmpty()) {
            return "The tail name cannot be empty!";
        }
        if (logPath == null || logPath.isEmpty()) {
            return "The log path cannot be empty!";
        }
        if (userInfo == null || userInfo.getUser() == null || userInfo.getUser().isEmpty()) {
            return "The user information is empty. Please provide the correct user information!";
        }
        if (!isCreate) {
            if (id == null || id < 0) {
                return "The tail id cannot be empty!";
            }
        }
        return null;
    }

}
