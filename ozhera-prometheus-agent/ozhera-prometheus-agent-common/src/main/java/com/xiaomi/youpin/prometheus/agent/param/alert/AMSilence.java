package com.xiaomi.youpin.prometheus.agent.param.alert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(callSuper = true)
public class AMSilence {
    private String startsAt;
    private String endsAt;
    private String comment;
    private List<Matcher> matchers;
    private String createdBy;
    private String id;
    private AMSilenceStatus status;
}
